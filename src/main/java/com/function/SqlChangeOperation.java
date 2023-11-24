package com.function;

import com.google.gson.annotations.SerializedName;

// This trigger code is utilised from the Microsoft Learn Tutorial for the
// Azure SQL Trigger for Functions
// Available: https://learn.microsoft.com/en-us/azure/azure-functions/functions-bindings-azure-sql-trigger?tabs=isolated-process%2Cportal&pivots=programming-language-java
public enum SqlChangeOperation {
    @SerializedName("0")
    Insert,
    @SerializedName("1")
    Update,
    @SerializedName("2")
    Delete;
}
