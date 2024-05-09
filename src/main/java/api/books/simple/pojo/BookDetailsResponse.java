package api.books.simple.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class BookDetailsResponse {
    private int id;
    private String name;
    private String author;
    private String isbn;
    private String type;
    private double price;
    @JsonProperty("current-stock")
    private int currentStock;
    private boolean available;
}
