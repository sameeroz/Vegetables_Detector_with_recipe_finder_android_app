package com.sameer.vegetablesdetector.DatabaseHandler;

public class Recipe {
    int id;
    String recipeName;
    String recipeIngredients;
    String recipePreparation;

    Recipe()
    {

    }

    public Recipe(int id, String recipeName, String recipeIngredients, String recipePreparation) {
        this.id = id;
        this.recipeName = recipeName;
        this.recipeIngredients = recipeIngredients;
        this.recipePreparation = recipePreparation;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRecipeName() {
        return recipeName;
    }

    public void setRecipeName(String recipeName) {
        this.recipeName = recipeName;
    }

    public String getRecipeIngredients() {
        return recipeIngredients;
    }

    public void setRecipeIngredients(String recipeIngredients) {
        this.recipeIngredients = recipeIngredients;
    }

    public String getRecipePreparation() {
        return recipePreparation;
    }

    public void setRecipePreparation(String recipePreparation) {
        this.recipePreparation = recipePreparation;
    }
}
