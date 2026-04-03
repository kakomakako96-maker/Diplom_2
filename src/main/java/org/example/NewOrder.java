package org.example;


import java.util.ArrayList;
import java.util.List;

public class NewOrder {

    public List<String> ingredients;

    public NewOrder(List<String> ingredients) {
        this.ingredients = new ArrayList<>(ingredients);;

    }
}
