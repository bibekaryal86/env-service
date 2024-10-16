package env.service.app.model;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document
public class EnvDetails implements Serializable {
  @MongoId private ObjectId id;
  private String name;
  private String stringValue;
  private List<String> listValue;
  private Map<String, String> mapValue;
}
