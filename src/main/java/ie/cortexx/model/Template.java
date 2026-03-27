package ie.cortexx.model;

import java.time.LocalDateTime;

// maps to `templates`
// stores reminder/receipt/statement templates with {placeholders}
// content is TEXT in schema
public class Template {
    private int templateId;
    private String templateType;
    private String content;
    private LocalDateTime updatedAt;
    // nullable, system templates have no user
    private Integer updatedBy;

    // TODO: generate getters & setters
}
