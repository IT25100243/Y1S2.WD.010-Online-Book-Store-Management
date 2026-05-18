package com.pageturner.model;

/**
 * INHERITANCE: DigitalBook extends Book extends BookItem extends BaseEntity (4-level chain).
 * POLYMORPHISM: Overrides getStockStatus(), getStockClass(), isAvailable(), toJson().
 *               Digital books never run out of stock.
 * ENCAPSULATION: fileFormat is private with getter only.
 */
public class DigitalBook extends Book {

    // ENCAPSULATION: private — read-only after construction
    private final String fileFormat;
    private final String downloadUrl;

    public DigitalBook(String id, String category, String title, String author,
                       double price, int pages, String imageUrl,
                       String description, String fileFormat, String downloadUrl) {
        // Stock is set to Integer.MAX_VALUE internally — unlimited
        super(id, category, title, author, price, pages, Integer.MAX_VALUE, imageUrl, description);
        this.fileFormat   = fileFormat;
        this.downloadUrl  = downloadUrl;
    }

    public String getFileFormat()  { return fileFormat; }
    public String getDownloadUrl() { return downloadUrl; }

    // POLYMORPHISM: Digital books are ALWAYS available
    @Override
    public String getStockStatus() { return "Digital — Always Available"; }

    @Override
    public String getStockClass()  { return "stock-ok"; }

    @Override
    public boolean isAvailable()   { return true; }

    // POLYMORPHISM: DigitalBook type label
    @Override
    public String getItemType()    { return "DigitalBook"; }

    @Override
    public String getEntityType()  { return "DigitalBook"; }

    // POLYMORPHISM: DigitalBook-specific JSON
    @Override
    public String toJson() {
        return String.format(
            "{\"id\":\"%s\",\"cat\":\"%s\",\"title\":\"%s\",\"author\":\"%s\"," +
            "\"price\":%.0f,\"pages\":%d,\"stock\":9999,\"img\":\"%s\"," +
            "\"desc\":\"%s\",\"type\":\"digital\",\"fileFormat\":\"%s\"}",
            getId(), getCategory(), escape(getTitle()), escape(getAuthor()),
            getPrice(), getPages(), getImageUrl(), escape(getDescription()), fileFormat
        );
    }

    @Override
    public String toString() {
        return "[DigitalBook \"" + getTitle() + "\" format=" + fileFormat +
               " " + getFormattedPrice() + "]";
    }
}
