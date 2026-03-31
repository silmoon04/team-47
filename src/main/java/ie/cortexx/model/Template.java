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

    public Template() {}

    public Template(String templateType, String content) {
        this.templateType = templateType;
        this.content = content;
    }

    public int getTemplateId() { return templateId; }
    public void setTemplateId(int templateId) { this.templateId = templateId; }

    public String getTemplateType() { return templateType; }
    public void setTemplateType(String templateType) { this.templateType = templateType; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Integer getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(Integer updatedBy) { this.updatedBy = updatedBy; }
}
