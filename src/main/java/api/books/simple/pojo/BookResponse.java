package api.books.simple.pojo;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class BookResponse {
    private Integer id;
    private String name;
    private String type;
    private boolean available;
}

