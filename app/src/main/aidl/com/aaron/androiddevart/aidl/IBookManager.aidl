// IBookManager.aidl
package com.aaron.androiddevart.aidl;

// Declare any non-default types here with import statements
import com.aaron.androiddevart.aidl.Book;

interface IBookManager {
    List<Book> getBookList();
    void addBook(in Book book);
}
