package it.unisa.entity;

import java.io.Serializable;
import javax.persistence.*;
import java.util.Date;


/**
 * The persistent class for the Books database table.
 * 
 */
@Entity
@Table(name="Books")
@NamedQuery(name="Book.findAll", query="SELECT b FROM Book b")
public class Book implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="idBook")
	private int idBook;

	@Column(name="author")
	private String author;

	@Column(name="isbn")	
	private String isbn;

	@Column(name="price")
	private float price;

	@Temporal(TemporalType.DATE)
	@Column(name="publishDate")
	private Date publishDate;
	
	@Column(name="title")
	private String title;

	public Book() {
	}

	public int getIdBook() {
		return this.idBook;
	}

	public void setIdBook(int idBook) {
		this.idBook = idBook;
	}

	public String getAuthor() {
		return this.author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getIsbn() {
		return this.isbn;
	}

	public void setIsbn(String isbn) {
		this.isbn = isbn;
	}

	public float getPrice() {
		return this.price;
	}

	public void setPrice(float price) {
		this.price = price;
	}

	public Date getPublishDate() {
		return this.publishDate;
	}

	public void setPublishDate(Date publishDate) {
		this.publishDate = publishDate;
	}

	public String getTitle() {
		return this.title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

}