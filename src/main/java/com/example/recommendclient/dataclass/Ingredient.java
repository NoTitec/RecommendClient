package com.example.recommendclient.dataclass;

import javafx.beans.property.SimpleStringProperty;

public class Ingredient {
    private SimpleStringProperty ingredientName;
    private SimpleStringProperty ingredientLink;

    public Ingredient() {
        ingredientName = new SimpleStringProperty();
        ingredientLink = new SimpleStringProperty();
    }

    public Ingredient(String ingredientName, String ingredientLink) {
        this.ingredientName = new SimpleStringProperty(ingredientName);
        this.ingredientLink = new SimpleStringProperty(ingredientLink);
    }

    public String getIngredientName() {
        return ingredientName.get();
    }

    public void setIngredientName(String ingredientName) {
        this.ingredientName.set(ingredientName);
    }

    public String getIngredientLink() {
        return ingredientLink.get();
    }

    public void setIngredientLink(String ingredientLink) {
        this.ingredientLink.set(ingredientLink);
    }
}
