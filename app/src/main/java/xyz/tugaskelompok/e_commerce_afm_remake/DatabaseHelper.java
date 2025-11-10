package xyz.tugaskelompok.e_commerce_afm_remake;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "ecommerce_afm.db";
    private static final int DATABASE_VERSION = 4;

    public static final String TABLE_USERS = "users";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_ROLE = "role"; // "user" or "admin"
    public static final String COLUMN_ADDRESS = "address";
    public static final String COLUMN_PHONE = "phone";

    public static final String TABLE_PRODUCTS = "products";
    public static final String COLUMN_PRODUCT_ID = "id";
    public static final String COLUMN_PRODUCT_NAME = "name";
    public static final String COLUMN_PRODUCT_CATEGORY = "category";
    public static final String COLUMN_PRODUCT_PRICE = "price";
    public static final String COLUMN_PRODUCT_IMAGE_URL = "image_url";
    public static final String COLUMN_PRODUCT_DESCRIPTION = "description";
    public static final String COLUMN_PRODUCT_STOCK = "stock";

    public static final String TABLE_ORDERS = "orders";
    public static final String COLUMN_ORDER_ID = "id";
    public static final String COLUMN_ORDER_USER_EMAIL = "user_email";
    public static final String COLUMN_ORDER_USER_NAME = "user_name";
    public static final String COLUMN_ORDER_ADDRESS = "address";
    public static final String COLUMN_ORDER_PHONE = "phone";
    public static final String COLUMN_ORDER_ITEMS = "items"; // JSON string
    public static final String COLUMN_ORDER_TOTAL = "total";
    public static final String COLUMN_ORDER_SHIPPING = "shipping";
    public static final String COLUMN_ORDER_DATE = "order_date";
    public static final String COLUMN_ORDER_STATUS = "status";

    public static final String TABLE_ORDER_STATUS_UPDATES = "order_status_updates";
    public static final String COLUMN_STATUS_UPDATE_ID = "id";
    public static final String COLUMN_STATUS_UPDATE_ORDER_ID = "order_id";
    public static final String COLUMN_STATUS_UPDATE_TEXT = "status_text";
    public static final String COLUMN_STATUS_UPDATE_TIMESTAMP = "timestamp";

    private static final String SQL_CREATE_USERS =
            "CREATE TABLE IF NOT EXISTS " + TABLE_USERS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_NAME + " TEXT NOT NULL, " +
                    COLUMN_EMAIL + " TEXT NOT NULL UNIQUE, " +
                    COLUMN_PASSWORD + " TEXT NOT NULL, " +
                    COLUMN_ROLE + " TEXT NOT NULL CHECK(" + COLUMN_ROLE + " IN ('user','admin'))" +
            ")";

    private static final String SQL_CREATE_PRODUCTS =
            "CREATE TABLE IF NOT EXISTS " + TABLE_PRODUCTS + " (" +
                    COLUMN_PRODUCT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_PRODUCT_NAME + " TEXT NOT NULL, " +
                    COLUMN_PRODUCT_CATEGORY + " TEXT NOT NULL, " +
                    COLUMN_PRODUCT_PRICE + " REAL NOT NULL, " +
                    COLUMN_PRODUCT_IMAGE_URL + " TEXT, " +
                    COLUMN_PRODUCT_DESCRIPTION + " TEXT, " +
                    COLUMN_PRODUCT_STOCK + " INTEGER DEFAULT 0" +
            ")";

    private static final String SQL_CREATE_ORDERS =
            "CREATE TABLE IF NOT EXISTS " + TABLE_ORDERS + " (" +
                    COLUMN_ORDER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_ORDER_USER_EMAIL + " TEXT NOT NULL, " +
                    COLUMN_ORDER_USER_NAME + " TEXT NOT NULL, " +
                    COLUMN_ORDER_ADDRESS + " TEXT NOT NULL, " +
                    COLUMN_ORDER_PHONE + " TEXT NOT NULL, " +
                    COLUMN_ORDER_ITEMS + " TEXT NOT NULL, " +
                    COLUMN_ORDER_TOTAL + " REAL NOT NULL, " +
                    COLUMN_ORDER_SHIPPING + " REAL NOT NULL, " +
                    COLUMN_ORDER_DATE + " TEXT NOT NULL, " +
                    COLUMN_ORDER_STATUS + " TEXT DEFAULT 'pending'" +
            ")";

    private static final String SQL_CREATE_ORDER_STATUS_UPDATES =
            "CREATE TABLE IF NOT EXISTS " + TABLE_ORDER_STATUS_UPDATES + " (" +
                    COLUMN_STATUS_UPDATE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_STATUS_UPDATE_ORDER_ID + " INTEGER NOT NULL, " +
                    COLUMN_STATUS_UPDATE_TEXT + " TEXT NOT NULL, " +
                    COLUMN_STATUS_UPDATE_TIMESTAMP + " TEXT NOT NULL, " +
                    "FOREIGN KEY(" + COLUMN_STATUS_UPDATE_ORDER_ID + ") REFERENCES " + TABLE_ORDERS + "(" + COLUMN_ORDER_ID + ") ON DELETE CASCADE" +
            ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_USERS);
        db.execSQL(SQL_CREATE_PRODUCTS);
        db.execSQL(SQL_CREATE_ORDERS);
        db.execSQL(SQL_CREATE_ORDER_STATUS_UPDATES);
        seedDefaultAdmin(db);
        seedSampleProducts(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL(SQL_CREATE_PRODUCTS);
            seedSampleProducts(db);
        }
        if (oldVersion < 3) {
            // Add address and phone columns to users table
            try {
                db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COLUMN_ADDRESS + " TEXT");
            } catch (Exception e) {
                // Column might already exist
            }
            try {
                db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COLUMN_PHONE + " TEXT");
            } catch (Exception e) {
                // Column might already exist
            }
            // Create orders table
            db.execSQL(SQL_CREATE_ORDERS);
        }
        if (oldVersion < 4) {
            db.execSQL(SQL_CREATE_ORDER_STATUS_UPDATES);
        }
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        seedDefaultAdmin(db);
    }

    public boolean isEmailExists(String email) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{COLUMN_ID},
                COLUMN_EMAIL + " = ?",
                new String[]{email},
                null,
                null,
                null
        );
        boolean exists = cursor != null && cursor.moveToFirst();
        if (cursor != null) cursor.close();
        return exists;
    }

    public boolean registerUser(String name, String email, String password, String role) {
        if (isEmailExists(email)) return false;
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PASSWORD, password);
        values.put(COLUMN_ROLE, role);
        long id = db.insert(TABLE_USERS, null, values);
        return id != -1;
    }

    public String authenticateAndGetRole(String email, String password) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{COLUMN_ROLE},
                COLUMN_EMAIL + " = ? AND " + COLUMN_PASSWORD + " = ?",
                new String[]{email, password},
                null,
                null,
                null
        );
        String role = null;
        if (cursor != null && cursor.moveToFirst()) {
            role = cursor.getString(0);
        }
        if (cursor != null) cursor.close();
        return role; // null if not found
    }

    public String[] getUserInfoByEmail(String email) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{COLUMN_NAME, COLUMN_EMAIL, COLUMN_ADDRESS, COLUMN_PHONE},
                COLUMN_EMAIL + " = ?",
                new String[]{email},
                null,
                null,
                null
        );
        String[] userInfo = null;
        if (cursor != null && cursor.moveToFirst()) {
            userInfo = new String[4];
            userInfo[0] = cursor.getString(0); // name
            userInfo[1] = cursor.getString(1); // email
            userInfo[2] = cursor.getString(2); // address (might be null)
            userInfo[3] = cursor.getString(3); // phone (might be null)
        }
        if (cursor != null) cursor.close();
        return userInfo;
    }

    public boolean updateUserProfile(String email, String address, String phone) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ADDRESS, address);
        values.put(COLUMN_PHONE, phone);
        int rowsAffected = db.update(TABLE_USERS, values, COLUMN_EMAIL + " = ?", new String[]{email});
        return rowsAffected > 0;
    }

    public long createOrder(String userEmail, String userName, String address, String phone, String itemsJson, double total, double shipping) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ORDER_USER_EMAIL, userEmail);
        values.put(COLUMN_ORDER_USER_NAME, userName);
        values.put(COLUMN_ORDER_ADDRESS, address);
        values.put(COLUMN_ORDER_PHONE, phone);
        values.put(COLUMN_ORDER_ITEMS, itemsJson);
        values.put(COLUMN_ORDER_TOTAL, total);
        values.put(COLUMN_ORDER_SHIPPING, shipping);
        values.put(COLUMN_ORDER_DATE, String.valueOf(System.currentTimeMillis()));
        values.put(COLUMN_ORDER_STATUS, "pending");
        long orderId = db.insert(TABLE_ORDERS, null, values);
        if (orderId != -1) {
            addOrderStatusUpdate((int) orderId, "Pesanan dibuat");
        }
        return orderId;
    }

    public List<Order> getOrdersByUserEmail(String userEmail) {
        List<Order> orders = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_ORDERS,
                null,
                COLUMN_ORDER_USER_EMAIL + " = ?",
                new String[]{userEmail},
                null,
                null,
                COLUMN_ORDER_DATE + " DESC"
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Order order = cursorToOrder(cursor);
                orders.add(order);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return orders;
    }

    public Order getOrderById(int orderId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_ORDERS,
                null,
                COLUMN_ORDER_ID + " = ?",
                new String[]{String.valueOf(orderId)},
                null,
                null,
                null
        );

        Order order = null;
        if (cursor != null && cursor.moveToFirst()) {
            order = cursorToOrder(cursor);
            cursor.close();
        }
        return order;
    }

    public List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_ORDERS,
                null,
                null,
                null,
                null,
                null,
                COLUMN_ORDER_DATE + " DESC"
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Order order = cursorToOrder(cursor);
                orders.add(order);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return orders;
    }

    public boolean updateOrderStatus(int orderId, String status) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ORDER_STATUS, status);
        int rows = db.update(TABLE_ORDERS, values, COLUMN_ORDER_ID + " = ?", new String[]{String.valueOf(orderId)});
        return rows > 0;
    }

    public long addOrderStatusUpdate(int orderId, String statusText) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_STATUS_UPDATE_ORDER_ID, orderId);
        values.put(COLUMN_STATUS_UPDATE_TEXT, statusText);
        values.put(COLUMN_STATUS_UPDATE_TIMESTAMP, String.valueOf(System.currentTimeMillis()));
        long id = db.insert(TABLE_ORDER_STATUS_UPDATES, null, values);
        if (id != -1) {
            updateOrderStatus(orderId, statusText);
        }
        return id;
    }

    public boolean updateOrderStatusUpdate(int statusUpdateId, String statusText) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_STATUS_UPDATE_TEXT, statusText);
        values.put(COLUMN_STATUS_UPDATE_TIMESTAMP, String.valueOf(System.currentTimeMillis()));
        int rows = db.update(TABLE_ORDER_STATUS_UPDATES, values, COLUMN_STATUS_UPDATE_ID + " = ?", new String[]{String.valueOf(statusUpdateId)});
        if (rows > 0) {
            Cursor cursor = db.query(
                    TABLE_ORDER_STATUS_UPDATES,
                    new String[]{COLUMN_STATUS_UPDATE_ORDER_ID},
                    COLUMN_STATUS_UPDATE_ID + " = ?",
                    new String[]{String.valueOf(statusUpdateId)},
                    null,
                    null,
                    null
            );
            if (cursor != null && cursor.moveToFirst()) {
                int orderId = cursor.getInt(0);
                cursor.close();
                syncOrderStatusWithLatest(orderId);
            }
        }
        return rows > 0;
    }

    public boolean deleteOrderStatusUpdate(int statusUpdateId) {
        SQLiteDatabase db = getWritableDatabase();
        Integer orderId = null;
        Cursor cursor = db.query(
                TABLE_ORDER_STATUS_UPDATES,
                new String[]{COLUMN_STATUS_UPDATE_ORDER_ID},
                COLUMN_STATUS_UPDATE_ID + " = ?",
                new String[]{String.valueOf(statusUpdateId)},
                null,
                null,
                null
        );
        if (cursor != null && cursor.moveToFirst()) {
            orderId = cursor.getInt(0);
            cursor.close();
        }
        int rows = db.delete(TABLE_ORDER_STATUS_UPDATES, COLUMN_STATUS_UPDATE_ID + " = ?", new String[]{String.valueOf(statusUpdateId)});
        if (rows > 0 && orderId != null) {
            syncOrderStatusWithLatest(orderId);
        }
        return rows > 0;
    }

    public List<OrderStatusUpdate> getOrderStatusUpdates(int orderId) {
        List<OrderStatusUpdate> statuses = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_ORDER_STATUS_UPDATES,
                null,
                COLUMN_STATUS_UPDATE_ORDER_ID + " = ?",
                new String[]{String.valueOf(orderId)},
                null,
                null,
                COLUMN_STATUS_UPDATE_TIMESTAMP + " ASC"
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                OrderStatusUpdate statusUpdate = cursorToStatusUpdate(cursor);
                statuses.add(statusUpdate);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return statuses;
    }

    private void syncOrderStatusWithLatest(int orderId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_ORDER_STATUS_UPDATES,
                new String[]{COLUMN_STATUS_UPDATE_TEXT},
                COLUMN_STATUS_UPDATE_ORDER_ID + " = ?",
                new String[]{String.valueOf(orderId)},
                null,
                null,
                COLUMN_STATUS_UPDATE_TIMESTAMP + " DESC",
                "1"
        );
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                String latestStatus = cursor.getString(0);
                updateOrderStatus(orderId, latestStatus);
            } else {
                updateOrderStatus(orderId, "pending");
            }
            cursor.close();
        }
    }

    private Order cursorToOrder(Cursor cursor) {
        Order order = new Order();
        order.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ORDER_ID)));
        order.setUserEmail(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ORDER_USER_EMAIL)));
        order.setUserName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ORDER_USER_NAME)));
        order.setAddress(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ORDER_ADDRESS)));
        order.setPhone(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ORDER_PHONE)));
        order.setItemsJson(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ORDER_ITEMS)));
        order.setTotal(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_ORDER_TOTAL)));
        order.setShipping(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_ORDER_SHIPPING)));
        order.setOrderDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ORDER_DATE)));
        order.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ORDER_STATUS)));
        return order;
    }

    private OrderStatusUpdate cursorToStatusUpdate(Cursor cursor) {
        OrderStatusUpdate statusUpdate = new OrderStatusUpdate();
        statusUpdate.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_STATUS_UPDATE_ID)));
        statusUpdate.setOrderId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_STATUS_UPDATE_ORDER_ID)));
        statusUpdate.setStatusText(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STATUS_UPDATE_TEXT)));
        statusUpdate.setTimestamp(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STATUS_UPDATE_TIMESTAMP)));
        return statusUpdate;
    }

    private void seedDefaultAdmin(SQLiteDatabase db) {
        Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{COLUMN_ID},
                COLUMN_ROLE + " = ?",
                new String[]{"admin"},
                null,
                null,
                null
        );
        boolean hasAdmin = cursor != null && cursor.moveToFirst();
        if (cursor != null) cursor.close();
        if (!hasAdmin) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_NAME, "Administrator");
            values.put(COLUMN_EMAIL, "admin@gmail.com");
            values.put(COLUMN_PASSWORD, "admin");
            values.put(COLUMN_ROLE, "admin");
            db.insert(TABLE_USERS, null, values);
        }
    }

    private void seedSampleProducts(SQLiteDatabase db) {
        // Check if products already exist
        Cursor cursor = db.query(
                TABLE_PRODUCTS,
                new String[]{COLUMN_PRODUCT_ID},
                null,
                null,
                null,
                null,
                null
        );
        boolean hasProducts = cursor != null && cursor.getCount() > 0;
        if (cursor != null) cursor.close();
        
        if (hasProducts) return; // Don't seed if products already exist

        // Sample CPU products
        insertProduct(db, "Intel Core i5-12400F", "CPU", 2500000, "", "Intel Core i5-12400F Processor", 10);
        insertProduct(db, "AMD Ryzen 5 5600X", "CPU", 3200000, "", "AMD Ryzen 5 5600X Processor", 8);
        insertProduct(db, "Intel Core i7-12700K", "CPU", 5500000, "", "Intel Core i7-12700K Processor", 5);
        
        // Sample GPU products
        insertProduct(db, "NVIDIA RTX 3060", "GPU", 6500000, "", "NVIDIA GeForce RTX 3060", 6);
        insertProduct(db, "AMD RX 6600 XT", "GPU", 5500000, "", "AMD Radeon RX 6600 XT", 7);
        insertProduct(db, "NVIDIA RTX 4070", "GPU", 12000000, "", "NVIDIA GeForce RTX 4070", 4);
        
        // Sample Case products
        insertProduct(db, "Corsair 4000D", "Case", 1500000, "", "Corsair 4000D Airflow Case", 12);
        insertProduct(db, "NZXT H510", "Case", 1200000, "", "NZXT H510 Mid Tower Case", 10);
        
        // Sample SSD products
        insertProduct(db, "Samsung 980 PRO 1TB", "SSD", 2500000, "", "Samsung 980 PRO 1TB NVMe SSD", 15);
        insertProduct(db, "WD Black SN850 1TB", "SSD", 2200000, "", "WD Black SN850 1TB NVMe SSD", 12);
        
        // Sample RAM products
        insertProduct(db, "Corsair Vengeance 16GB DDR4", "RAM", 1200000, "", "Corsair Vengeance LPX 16GB DDR4", 20);
        insertProduct(db, "G.Skill Trident Z 32GB DDR4", "RAM", 2500000, "", "G.Skill Trident Z 32GB DDR4", 8);
        
        // Sample PSU products
        insertProduct(db, "Corsair RM750x 750W", "PSU", 2000000, "", "Corsair RM750x 750W 80+ Gold", 10);
        insertProduct(db, "Seasonic Focus GX-650", "PSU", 1800000, "", "Seasonic Focus GX-650 80+ Gold", 9);
    }

    private void insertProduct(SQLiteDatabase db, String name, String category, double price, String imageUrl, String description, int stock) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_PRODUCT_NAME, name);
        values.put(COLUMN_PRODUCT_CATEGORY, category);
        values.put(COLUMN_PRODUCT_PRICE, price);
        values.put(COLUMN_PRODUCT_IMAGE_URL, imageUrl);
        values.put(COLUMN_PRODUCT_DESCRIPTION, description);
        values.put(COLUMN_PRODUCT_STOCK, stock);
        db.insert(TABLE_PRODUCTS, null, values);
    }

    // Product CRUD methods
    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_PRODUCTS,
                null,
                null,
                null,
                null,
                null,
                COLUMN_PRODUCT_NAME + " ASC"
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Product product = cursorToProduct(cursor);
                products.add(product);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return products;
    }

    public List<Product> getProductsByCategory(String category) {
        List<Product> products = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_PRODUCTS,
                null,
                COLUMN_PRODUCT_CATEGORY + " = ?",
                new String[]{category},
                null,
                null,
                COLUMN_PRODUCT_NAME + " ASC"
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Product product = cursorToProduct(cursor);
                products.add(product);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return products;
    }

    public List<Product> searchProducts(String searchQuery) {
        List<Product> products = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String query = "%" + searchQuery + "%";
        Cursor cursor = db.query(
                TABLE_PRODUCTS,
                null,
                COLUMN_PRODUCT_NAME + " LIKE ?",
                new String[]{query},
                null,
                null,
                COLUMN_PRODUCT_NAME + " ASC"
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Product product = cursorToProduct(cursor);
                products.add(product);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return products;
    }

    public List<Product> getProductsByCategoryAndSearch(String category, String searchQuery) {
        List<Product> products = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String query = "%" + searchQuery + "%";
        String selection = COLUMN_PRODUCT_NAME + " LIKE ?";
        String[] selectionArgs = new String[]{query};

        if (category != null && !category.isEmpty()) {
            selection += " AND " + COLUMN_PRODUCT_CATEGORY + " = ?";
            selectionArgs = new String[]{query, category};
        }

        Cursor cursor = db.query(
                TABLE_PRODUCTS,
                null,
                selection,
                selectionArgs,
                null,
                null,
                COLUMN_PRODUCT_NAME + " ASC"
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Product product = cursorToProduct(cursor);
                products.add(product);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return products;
    }

    public Product getProductById(int id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_PRODUCTS,
                null,
                COLUMN_PRODUCT_ID + " = ?",
                new String[]{String.valueOf(id)},
                null,
                null,
                null
        );

        Product product = null;
        if (cursor != null && cursor.moveToFirst()) {
            product = cursorToProduct(cursor);
            cursor.close();
        }
        return product;
    }

    private Product cursorToProduct(Cursor cursor) {
        Product product = new Product();
        product.setId(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_ID))));
        product.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_NAME)));
        product.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_CATEGORY)));
        product.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_PRICE)));
        product.setImageUrl(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_IMAGE_URL)));
        product.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_DESCRIPTION)));
        product.setStock(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_STOCK)));
        return product;
    }

    public boolean addProduct(String name, String category, double price, String imageUrl, String description, int stock) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PRODUCT_NAME, name);
        values.put(COLUMN_PRODUCT_CATEGORY, category);
        values.put(COLUMN_PRODUCT_PRICE, price);
        values.put(COLUMN_PRODUCT_IMAGE_URL, imageUrl);
        values.put(COLUMN_PRODUCT_DESCRIPTION, description);
        values.put(COLUMN_PRODUCT_STOCK, stock);
        long id = db.insert(TABLE_PRODUCTS, null, values);
        return id != -1;
    }

    public boolean updateProduct(int id, String name, String category, double price, String imageUrl, String description, int stock) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PRODUCT_NAME, name);
        values.put(COLUMN_PRODUCT_CATEGORY, category);
        values.put(COLUMN_PRODUCT_PRICE, price);
        values.put(COLUMN_PRODUCT_IMAGE_URL, imageUrl);
        values.put(COLUMN_PRODUCT_DESCRIPTION, description);
        values.put(COLUMN_PRODUCT_STOCK, stock);
        int rowsAffected = db.update(TABLE_PRODUCTS, values, COLUMN_PRODUCT_ID + " = ?", new String[]{String.valueOf(id)});
        return rowsAffected > 0;
    }

    public boolean deleteProduct(int id) {
        SQLiteDatabase db = getWritableDatabase();
        int rowsAffected = db.delete(TABLE_PRODUCTS, COLUMN_PRODUCT_ID + " = ?", new String[]{String.valueOf(id)});
        return rowsAffected > 0;
    }
}


