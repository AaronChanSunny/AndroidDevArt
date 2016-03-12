// IBookManager.aidl
package com.aaron.androiddevart.aidl;

// Declare any non-default types here with import statements
import com.aaron.androiddevart.aidl.Book;
import com.aaron.androiddevart.aidl.IOnNewBookArrivedListener;

interface IBookManager {
    List<Book>  getBookList();
    void addBook(in Book book);
    void registerListener(IOnNewBookArrivedListener listener);
    void unregisterListener(IOnNewBookArrivedListener listener);
}
