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

    /**
    * Creates a digital book with unlimited availability.
    *
    * Digital books inherit from Book but do not use
    * physical stock tracking. Stock is internally
    * assigned as Integer.MAX_VALUE.
    *
    * @param id unique book identifier
    * @param category book category
    * @param title book title
    * @param author author name
    * @param price digital book price
    * @param pages total number of pages
    * @param imageUrl cover image URL
    * @param description book description
    * @param fileFormat file type (PDF, EPUB, etc.)
    * @param downloadUrl download location
    */

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

    /**
    * Returns stock status for digital items.
    * Digital books never run out of stock.
    */
    @Override
    public String getStockStatus() { return "Digital — Always Available"; }

    @Override
    public String getStockClass()  { return "stock-ok"; }

    /**
   * Digital books are always available
   * because inventory is unlimited.
   */
    @Override
    public boolean isAvailable()   { return true; }

    // POLYMORPHISM: DigitalBook type label
    @Override
    public String getItemType()    { return "DigitalBook"; }

    @Override
    public String getEntityType()  { return "DigitalBook"; }

    /**
    * Converts DigitalBook object into JSON format
    * for storage and data transfer.
    */
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
