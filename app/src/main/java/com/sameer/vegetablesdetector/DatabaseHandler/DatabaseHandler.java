package com.sameer.vegetablesdetector.DatabaseHandler;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;


public class DatabaseHandler extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "FoodStore";
    private static final String TABLE_NAME = "recipes";
    private static final String KEY_ID = "id";
    private static final String RECIPE_NAME = "recipe_name";
    private static final String RECIPE_INGREDIENTS = "recipe_ingredients";
    private static final String RECIPE_PREPARATION = "recipe_preparation";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        //3rd argument to be passed is CursorFactory instance
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + RECIPE_NAME + " TEXT,"
                + RECIPE_INGREDIENTS + " TEXT,"
                +RECIPE_PREPARATION+ " TEXT" + ")";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);

        // Create tables again
        onCreate(db);
    }

    public void addRecipe(Recipe recipe) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(RECIPE_NAME, recipe.getRecipeName());
        values.put(RECIPE_INGREDIENTS, recipe.getRecipeIngredients());
        values.put(RECIPE_PREPARATION, recipe.getRecipePreparation());

        // Inserting Row
        db.insert(TABLE_NAME, null, values);
        //2nd argument is String containing nullColumnHack
        db.close(); // Closing database connection
    }

    public List<Recipe> getAllRecipes() {
        List<Recipe> recipeList = new ArrayList<Recipe>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_NAME;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Recipe recipe = new Recipe();
                recipe.setId(Integer.parseInt(cursor.getString(0)));
                recipe.setRecipeName(cursor.getString(1));
                recipe.setRecipeIngredients(cursor.getString(2));
                recipe.setRecipePreparation(cursor.getString(3));
                // Adding contact to list
                recipeList.add(recipe);
            } while (cursor.moveToNext());
        }

        return recipeList;
    }

    public List<Recipe> getSpecifiedRecipies(List<String> list) {

        List<Recipe> recipeList = null;
        String selectQuery = "SELECT  * FROM " +TABLE_NAME+" WHERE ";
        for (int i =0; i < list.size(); i++)
        {
            if(i == list.size() - 1 )
            {
                selectQuery+=RECIPE_INGREDIENTS+" like '%"+list.get(i)+"%'";
            }
            else
            {
                selectQuery+=RECIPE_INGREDIENTS+" like '%"+list.get(i)+"%' and ";
            }

        }
        System.out.println(selectQuery);

        // Select  Query

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            recipeList =new ArrayList<Recipe>();
            do {
                Recipe recipe = new Recipe();
                recipe.setId(Integer.parseInt(cursor.getString(0)));
                recipe.setRecipeName(cursor.getString(1));
                recipe.setRecipeIngredients(cursor.getString(2));
                recipe.setRecipePreparation(cursor.getString(3));
                // Adding contact to list
                recipeList.add(recipe);
            } while (cursor.moveToNext());
        }

        return recipeList;
    }

    public void deleteAllRecords()
    {
        String query = "delete from "+ TABLE_NAME;
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(query);
    }

}
