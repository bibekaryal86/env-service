package env.service.app.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConstantUtils {

  // provided at runtime
  public static final String SERVER_PORT = "PORT";
  public static final String AUTH_USR = "AUTH_USR";
  public static final String AUTH_PWD = "AUTH_PWD";
  public static final String MONGO_APP = "MONGO_APP";
  public static final String MONGO_DB = "MONGO_DB";
  public static final String MONGO_USR = "MONGO_USR";
  public static final String MONGO_PWD = "MONGO_PWD";

  // others
}
