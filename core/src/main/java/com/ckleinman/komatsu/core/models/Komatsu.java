package com.ckleinman.komatsu.core.models;

public interface Komatsu {
    /**
     * returns form input label
     * 
     * @return String
     */
    String getText();

    /**
     * returns form button text
     * 
     * @return String
     */
    String getButton();

    /**
     * returns component class name (could be extracted for better uses in the component.java file)
     * 
     * @return String
     */
    String getClassName();
}