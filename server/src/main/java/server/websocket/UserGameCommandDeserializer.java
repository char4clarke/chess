package server.websocket;

import com.google.gson.*;
import websocket.commands.ConnectCommand;
import websocket.commands.UserGameCommand;

import java.lang.reflect.Type;

public class UserGameCommandDeserializer implements JsonDeserializer<UserGameCommand> {
    @Override
    public UserGameCommand deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();
        String typeStr = obj.get("commandType").getAsString();
        return switch (UserGameCommand.CommandType.valueOf(typeStr)) {
            case CONNECT -> context.deserialize(json, ConnectCommand.class);

            default -> throw new JsonParseException("Unknown command type: " + typeStr);
        };
    }
}
