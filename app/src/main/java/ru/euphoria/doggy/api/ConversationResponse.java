package ru.euphoria.doggy.api;

import org.json.JSONObject;

import java.util.ArrayList;

import ru.euphoria.doggy.api.model.Community;
import ru.euphoria.doggy.api.model.Conversation;
import ru.euphoria.doggy.api.model.Message;
import ru.euphoria.doggy.api.model.User;

public class ConversationResponse extends Response {

    public ConversationResponse(JSONObject source) {
        super(source);
    }

    public ArrayList<User> users() {
        return VKApi.from(User.class, VKApi.optJsonArray(source, "profiles"));
    }

    public ArrayList<Community> groups() {
        return VKApi.from(Community.class, VKApi.optJsonArray(source, "groups"));
    }

    public ArrayList<Message> lastMessages() {
        return VKApi.from(Message.class, items, "last_message");
    }

    public ArrayList<Conversation> items() {
        return VKApi.from(Conversation.class, items, "conversation");
    }
}
