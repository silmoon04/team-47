package ie.cortexx.model;

import java.time.LocalDateTime;

// maps to `templates` table
public class Template {
    private int templateId;
    private String templateType;
    private String content;
    private LocalDateTime updatedAt;
    private int updatedBy;

    // TODO: generate getters & setters
}