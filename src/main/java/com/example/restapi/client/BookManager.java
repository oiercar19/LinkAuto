package com.example.restapi.client;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.example.restapi.model.Book;

public class BookManager {

    private String BOOK_CONTROLLER_URL_TEMPLATE = "http://%s:%s/api/books";
    private final String BOOK_CONTROLLER_URL;
    private final RestTemplate restTemplate;

    public BookManager(String hostname, String port) {
        BOOK_CONTROLLER_URL = String.format(BOOK_CONTROLLER_URL_TEMPLATE, hostname, port);
        this.restTemplate = new RestTemplate();
    }

    public void registerBook(Book book) {
        ResponseEntity<Void> response = restTemplate.postForEntity(BOOK_CONTROLLER_URL, book, Void.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            System.out.println("Book registered successfully.");
        } else {
            System.out.println("Failed to register book. Status code: " + response.getStatusCode());
        }
    }

    public List<Book> getAllBooks() {
        ResponseEntity<Book[]> response = restTemplate.getForEntity(BOOK_CONTROLLER_URL, Book[].class);

        if (response.getStatusCode().is2xxSuccessful()) {
            return List.of(response.getBody());
        } else {
            System.out.println("Failed to retrieve books. Status code: " + response.getStatusCode());
            return List.of();
        }
    }

    public void deleteBook(String bookId) {
        try {
            restTemplate.delete(BOOK_CONTROLLER_URL + "/" + bookId);
            System.out.println("Book deleted successfully.");
        } catch (RestClientException e)
        {
            System.out.println("Failed to delete book. " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.exit(0);
        }

        String hostname = args[0];
        String port = args[1];

        BookManager bookManager = new BookManager(hostname, port);
        java.util.Scanner scanner = new java.util.Scanner(System.in);

        while (true) {
            System.out.println("1. Register Book");
            System.out.println("2. List All Books");
            System.out.println("3. Delete Book");
            System.out.println("4. Exit");
            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline

            switch (choice) {
                case 1:
                    System.out.print("Enter book title: ");
                    String title = scanner.nextLine();
                    System.out.print("Enter book author: ");
                    String author = scanner.nextLine();
                    System.out.print("Enter book ISBN: ");
                    String isbn = scanner.nextLine();
                    Book book = new Book(title, author, isbn);
                    bookManager.registerBook(book);
                    break;
                case 2:
                    List<Book> books = bookManager.getAllBooks();
                    for (Book b : books) {
                        System.out.println("ID: " + b.getId());
                        System.out.println("Title: " + b.getTitle());
                        System.out.println("Author: " + b.getAuthor());
                        System.out.println("ISBN: " + b.getIsbn());
                        System.out.println("---------------------------");
                    }
                    break;
                case 3:
                    System.out.print("Enter book ID to delete: ");
                    String bookId = scanner.nextLine();
                    bookManager.deleteBook(bookId);
                    break;
                case 4:
                    scanner.close();
                    System.exit(0);
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
}
