package ru.otus.hw.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.dto.BookCreateDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookUpdateDto;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;
import ru.otus.hw.services.BookService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookController.class)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookService bookService;

    private static final ObjectMapper jsonMapper = new JsonMapper();

    private static final List<Genre> genres = new ArrayList<>();
    private static final List<Author> authors = new ArrayList<>();
    private static final List<Book> books = new ArrayList<>();

    @BeforeAll
    static void init() {
        genres.add(new Genre(1L, "Genre1"));
        genres.add(new Genre(2L, "Genre2"));
        authors.add(new Author(1L, "Author1"));
        authors.add(new Author(2L, "Author2"));
        books.add(new Book(1L, "Book1", authors.get(0), genres.get(0)));
        books.add(new Book(2L, "Book2", authors.get(1), genres.get(1)));
        books.add(new Book(3L, "Book3", authors.get(1), genres.get(1)));
    }

    @DisplayName("Должен проверить возврат списка книг")
    @Test
    void listBooksPage() throws Exception {
        final List<BookDto> bookDtos = books.stream()
                .map(this::getBookDtoByBook)
                .collect(Collectors.toList());

        final String expected = jsonMapper.writeValueAsString(bookDtos);

        when(bookService.findAll()).thenReturn(books);
        mockMvc.perform(get("/list/api/v1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expected));
    }

    @DisplayName("Должен проверить изменение книги")
    @Test
    void editBook() throws Exception {
        final Book book = books.get(0);
        final BookUpdateDto bookUpdateDtoByBook = getBookUpdateDtoByBook(book);
        final BookDto bookDto = getBookDtoByBook(book);
        final String expected = jsonMapper.writeValueAsString(bookDto);

        when(bookService.update(bookUpdateDtoByBook.getId(),
                bookUpdateDtoByBook.getTitle(),
                bookUpdateDtoByBook.getAuthorId(),
                bookUpdateDtoByBook.getGenreId())).thenReturn(book);

        final String input = jsonMapper.writeValueAsString(bookUpdateDtoByBook);

        mockMvc.perform(put("/edit/book/api/v1").contentType(MediaType.APPLICATION_JSON).content(input))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expected));
    }

    @DisplayName("Должен проверить валидацию title при изменении книги")
    @Test
    void editValidTitleBook() throws Exception {
        final Book book = books.get(0);
        final BookUpdateDto bookUpdateDtoByBook = getBookUpdateDtoByBook(book);
        bookUpdateDtoByBook.setTitle("tr");

        when(bookService.update(bookUpdateDtoByBook.getId(),
                bookUpdateDtoByBook.getTitle(),
                bookUpdateDtoByBook.getAuthorId(),
                bookUpdateDtoByBook.getGenreId())).thenReturn(book);

        final String input = jsonMapper.writeValueAsString(bookUpdateDtoByBook);

        mockMvc.perform(put("/edit/book/api/v1").contentType(MediaType.APPLICATION_JSON).content(input))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @DisplayName("Должен проверить добавление книги")
    @Test
    void createBook() throws Exception {
        final Book book = books.get(0);
        final BookCreateDto bookCreateDto = getBookCreateDtoByBook(book);
        final BookDto bookDto = getBookDtoByBook(book);
        final String expected = jsonMapper.writeValueAsString(bookDto);

        when(bookService.create(bookCreateDto.getTitle(),
                bookCreateDto.getAuthorId(),
                bookCreateDto.getGenreId())).thenReturn(book);

        final String input = jsonMapper.writeValueAsString(bookCreateDto);

        mockMvc.perform(post("/create/api/v1")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(input)
        ).andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expected));
    }

    @DisplayName("Должен проверить валидацию title при добавление книги")
    @Test
    void createValidTitleBook() throws Exception {
        final Book book = books.get(0);
        final BookCreateDto bookCreateDto = getBookCreateDtoByBook(book);
        bookCreateDto.setTitle("tr");

        when(bookService.create(bookCreateDto.getTitle(),
                bookCreateDto.getAuthorId(),
                bookCreateDto.getGenreId())).thenReturn(book);

        final String input = jsonMapper.writeValueAsString(bookCreateDto);

        mockMvc.perform(post("/create/api/v1").contentType(MediaType.APPLICATION_JSON_VALUE).content(input))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }


    @DisplayName("Должен удалить книгу")
    @Test
    void deleteBook() throws Exception {
        mockMvc.perform(delete(String.format("/delete/book/api/v1/%d", books.get(2).getId())))
                .andExpect(status().isOk());
    }

    private BookDto getBookDtoByBook(Book book) {
        final BookDto bookDto = new BookDto();
        bookDto.setId(book.getId());
        bookDto.setTitle(book.getTitle());
        final AuthorDto authorDto = new AuthorDto();
        authorDto.setId(book.getAuthor().getId());
        authorDto.setFullName(book.getAuthor().getFullName());
        bookDto.setAuthorDto(authorDto);
        final GenreDto genreDto = new GenreDto();
        genreDto.setId(book.getGenre().getId());
        genreDto.setName(book.getGenre().getName());
        bookDto.setGenreDto(genreDto);

        return bookDto;
    }

    private BookCreateDto getBookCreateDtoByBook(Book book) {
        final BookCreateDto bookCreateDto = new BookCreateDto();
        bookCreateDto.setTitle(book.getTitle());
        bookCreateDto.setAuthorId(book.getAuthor().getId());
        bookCreateDto.setGenreId(book.getGenre().getId());
        return bookCreateDto;
    }

    private BookUpdateDto getBookUpdateDtoByBook(Book book) {
        final BookUpdateDto bookUpdateDto = new BookUpdateDto();
        bookUpdateDto.setId(book.getId());
        bookUpdateDto.setTitle(book.getTitle());
        bookUpdateDto.setAuthorId(book.getAuthor().getId());
        bookUpdateDto.setGenreId(book.getGenre().getId());
        return bookUpdateDto;
    }
}