package env.service.app.model;

import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnvDetailsResponse implements Serializable {
  private List<EnvDetails> envDetails;
  private String errMsg;
}
