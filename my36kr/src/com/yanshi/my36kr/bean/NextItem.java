package com.yanshi.my36kr.bean;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Next栏目的每个item
 * Created by kingars on 2014/10/29.
 */
@DatabaseTable(tableName = "tb_next_item")
public class NextItem implements Serializable {

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(columnName = "title")
    private String title;

    @DatabaseField(columnName = "content")
    private String content;

    @DatabaseField(columnName = "voteCount")
    private int voteCount;

    @DatabaseField(columnName = "commentCount")
    private int commentCount;

    @DatabaseField(columnName = "url")
    private String url;

    @DatabaseField(columnName = "date")
    private String date;

    @DatabaseField(columnName = "objectId")
    private String objectId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getVoteCount() {
        return voteCount;
    }

    public void setVoteCount(int voteCount) {
        this.voteCount = voteCount;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public JSONObject toJSONObj() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", id);
            jsonObject.put("title", title);
            jsonObject.put("content", content);
            jsonObject.put("voteCount", voteCount);
            jsonObject.put("commentCount", commentCount);
            jsonObject.put("url", url);
            jsonObject.put("date", date);
            jsonObject.put("objectId", objectId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public static NextItem parse(JSONObject jsonObject) {
        if(jsonObject == null) {
            return null;
        }
        NextItem nextItem = new NextItem();
        nextItem.setId(jsonObject.optInt("id"));
        nextItem.setTitle(jsonObject.optString("title"));
        nextItem.setContent(jsonObject.optString("content"));
        nextItem.setVoteCount(jsonObject.optInt("voteCount"));
        nextItem.setCommentCount(jsonObject.optInt("commentCount"));
        nextItem.setDate(jsonObject.optString("date"));
        nextItem.setObjectId(jsonObject.optString("objectId"));
        return nextItem;
    }

}
