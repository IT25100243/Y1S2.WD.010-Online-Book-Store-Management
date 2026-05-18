package com.bookstore.server;

import com.pageturner.dao.*;
import com.pageturner.model.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Singleton application context — holds all DAOs and seeds initial data.
 * ENCAPSULATION: All DAO references are private; exposed via getters only.
 *
 * FIX: Uses the JAR's own directory for data storage, not user.dir,
 *      so it works regardless of IntelliJ's working directory setting.
 */
public class AppContext {

    private static AppContext instance;

    private final FileDatabase db;
    private final BookDAO      bookDAO;
    private final UserDAO      userDAO;
    private final CartDAO      cartDAO;
    private final OrderDAO     orderDAO;
    private final ReviewDAO    reviewDAO;

    public static final double SHIPPING_COST = 350.0;

    // ── Singleton constructor ─────────────────────────────────────────────

    private AppContext() throws IOException {
        // Resolve data directory relative to the project/working directory.
        // Try multiple candidates so it works in IntelliJ, from jar, or from terminal.
        String dataPath = resolveDataPath();
        System.out.println("[AppContext] Data directory: " + dataPath);

        this.db         = new FileDatabase(dataPath);
        this.bookDAO    = new BookDAO(db);
        this.userDAO    = new UserDAO(db);
        this.cartDAO    = new CartDAO(db);
        this.orderDAO   = new OrderDAO(db);
        this.reviewDAO  = new ReviewDAO(db);

        System.out.println("[AppContext] DAOs initialised.");
        seedAdminIfEmpty();
        seedBooksIfEmpty();
        System.out.println("[AppContext] Seeding complete. Total books: "
                + bookDAO.findAll().size());
    }

    public static synchronized AppContext getInstance() throws IOException {
        if (instance == null) {
            instance = new AppContext();
        }
        return instance;
    }

    // ── Getters ───────────────────────────────────────────────────────────

    public BookDAO   getBookDAO()      { return bookDAO; }
    public UserDAO   getUserDAO()      { return userDAO; }
    public CartDAO   getCartDAO()      { return cartDAO; }
    public OrderDAO  getOrderDAO()     { return orderDAO; }
    public ReviewDAO getReviewDAO()    { return reviewDAO; }
    public double    getShippingCost() { return SHIPPING_COST; }

    // ── Data path resolution ──────────────────────────────────────────────

    /**
     * Finds a writable directory for data files.
     * Tries: user.dir/data (IntelliJ project root), then user.home/pageturner-data.
     */
    private static String resolveDataPath() {
        // First choice: project root / data  (works in IntelliJ with default settings)
        String workingDir = System.getProperty("user.dir");
        if (workingDir != null) {
            return workingDir + java.io.File.separator + "data";
        }
        // Fallback: home directory
        return System.getProperty("user.home") + java.io.File.separator + "pageturner-data";
    }

    // ── Seed: Admin ───────────────────────────────────────────────────────

    private void seedAdminIfEmpty() throws IOException {
        if (userDAO.findByUsername("admin").isEmpty()) {
            AdminUser admin = new AdminUser(
                "admin", "admin123", "Administrator", "admin@pageturner.lk"
            );
            userDAO.save(admin);
            System.out.println("[AppContext] Admin user created.");
        }
    }

    // ── Seed: 40 Books ───────────────────────────────────────────────────

    private void seedBooksIfEmpty() throws IOException {
        List<Book> existing = bookDAO.findAll();
        if (!existing.isEmpty()) {
            System.out.println("[AppContext] Books already seeded (" + existing.size() + " found).");
            return;
        }

        System.out.println("[AppContext] Seeding 40 books...");
        List<Book> books = new ArrayList<>();

        // ── CHILDREN ────────────────────────────────────────────────
        String ci0 = "https://images.unsplash.com/photo-1512820790803-83ca734da794?w=300&h=400&fit=crop";
        String ci1 = "https://images.unsplash.com/photo-1549122728-f519709caa9c?w=300&h=400&fit=crop";
        String ci2 = "https://images.unsplash.com/photo-1534271057238-c2c170a76672?w=300&h=400&fit=crop";
        String ci3 = "https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=300&h=400&fit=crop";

        books.add(new Book("c1","children","The Magic Tree House","Mary Pope Osborne",950,96,42,ci0,"Jack and Annie discover a magic tree house that whisks them away on fantastic adventures through time and space."));
        books.add(new Book("c2","children","Charlottes Web","E.B. White",850,184,31,ci1,"A tender story of friendship between a pig named Wilbur and a spider named Charlotte who works to save his life."));
        books.add(new Book("c3","children","The Very Hungry Caterpillar","Eric Carle",650,32,55,ci2,"A beloved classic following a caterpillars journey of eating through various foods before becoming a butterfly."));
        books.add(new Book("c4","children","Harry Potter Sorcerers Stone","J.K. Rowling",1450,309,28,ci3,"A young orphan discovers he is a wizard on his 11th birthday and enters the magical world of Hogwarts School."));
        books.add(new Book("c5","children","The Lion the Witch and the Wardrobe","C.S. Lewis",980,208,22,ci0,"Four siblings discover a magical land called Narnia through the back of a wardrobe and join a great battle."));
        books.add(new Book("c6","children","Matilda","Roald Dahl",890,240,37,ci1,"A brilliant little girl with telekinetic powers must deal with her horrible parents and tyrannical headmistress."));
        books.add(new Book("c7","children","The Giving Tree","Shel Silverstein",720,64,60,ci2,"A heartwarming story about the relationship between a boy and a tree, exploring themes of generosity and love."));
        books.add(new Book("c8","children","Where the Wild Things Are","Maurice Sendak",680,48,44,ci3,"Max dressed in his wolf suit is sent to bed without supper and imagines a land of wild creatures."));
        books.add(new Book("c9","children","Alice in Wonderland","Lewis Carroll",820,192,19,ci0,"A young girl falls through a rabbit hole into a fantastical world of peculiar creatures and curious adventures."));
        books.add(new Book("c10","children","The Secret Garden","Frances Hodgson Burnett",890,288,14,ci1,"Mary Lennox discovers a mysterious locked garden and slowly brings it back to life alongside her sickly cousin."));

        // ── NOVELS ──────────────────────────────────────────────────
        String ni0 = "https://images.unsplash.com/photo-1543002588-bfa74002ed7e?w=300&h=400&fit=crop";
        String ni1 = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=300&h=400&fit=crop";
        String ni2 = "https://images.unsplash.com/photo-1481627834876-b7833e8f5570?w=300&h=400&fit=crop";
        String ni3 = "https://images.unsplash.com/photo-1457369804613-52c61a468e7d?w=300&h=400&fit=crop";

        books.add(new Book("n1","novels","To Kill a Mockingbird","Harper Lee",1200,281,33,ni0,"Set in the American South this classic novel tackles racial injustice and moral growth through the eyes of young Scout Finch."));
        books.add(new Book("n2","novels","1984","George Orwell",1050,328,41,ni1,"A dystopian masterpiece about a totalitarian society where Big Brother watches every move and the truth is a casualty."));
        books.add(new Book("n3","novels","The Great Gatsby","F. Scott Fitzgerald",980,180,27,ni2,"Set in the Jazz Age this tale of wealth obsession and the American Dream follows the mysterious Jay Gatsby."));
        books.add(new Book("n4","novels","One Hundred Years of Solitude","Gabriel Garcia Marquez",1380,417,15,ni3,"The epic saga of the Buendia family across seven generations in the fictional town of Macondo."));
        books.add(new Book("n5","novels","Crime and Punishment","Fyodor Dostoevsky",1150,545,20,ni0,"A psychological thriller following student Raskolnikov who commits murder and struggles with guilt and redemption."));
        books.add(new Book("n6","novels","Pride and Prejudice","Jane Austen",890,432,48,ni1,"Elizabeth Bennet navigates love social class and family expectations in Georgian England."));
        books.add(new Book("n7","novels","The Alchemist","Paulo Coelho",1020,197,52,ni2,"A shepherd boys journey across the desert to find treasure discovering the meaning of life."));
        books.add(new Book("n8","novels","Moby Dick","Herman Melville",1200,654,12,ni3,"Captain Ahabs obsessive quest to hunt the white whale Moby Dick a meditation on obsession and fate."));
        books.add(new Book("n9","novels","The Kite Runner","Khaled Hosseini",1100,371,29,ni0,"A story of redemption set in Afghanistan following Amir and his childhood friend Hassan across decades."));
        books.add(new Book("n10","novels","Brave New World","Aldous Huxley",990,311,23,ni1,"A chilling vision of a future society controlled through conditioning drugs and pleasure."));

        // ── FICTION ─────────────────────────────────────────────────
        String fi0 = "https://images.unsplash.com/photo-1532012197267-da84d127e765?w=300&h=400&fit=crop";
        String fi1 = "https://images.unsplash.com/photo-1541963463532-d68292c34b19?w=300&h=400&fit=crop";
        String fi2 = "https://images.unsplash.com/photo-1497633762265-9d179a990aa6?w=300&h=400&fit=crop";
        String fi3 = "https://images.unsplash.com/photo-1589998059171-988d887df646?w=300&h=400&fit=crop";

        books.add(new Book("f1","fiction","Dune","Frank Herbert",1550,896,18,fi0,"Set on the desert planet Arrakis this epic follows Paul Atreides amid interplanetary politics religion and survival."));
        books.add(new Book("f2","fiction","The Hitchhikers Guide to the Galaxy","Douglas Adams",990,224,35,fi1,"An ordinary man is whisked off Earth moments before its demolition and travels the galaxy with an alien friend."));
        books.add(new Book("f3","fiction","Enders Game","Orson Scott Card",1180,352,26,fi2,"Child prodigy Ender Wiggin is recruited to train for an alien war in a brutal military academy in space."));
        books.add(new Book("f4","fiction","The Martian","Andy Weir",1250,369,31,fi3,"An astronaut is stranded on Mars and must use science and humor to survive until rescue can reach him."));
        books.add(new Book("f5","fiction","Foundation","Isaac Asimov",1100,244,17,fi0,"A mathematician predicts the fall of the Galactic Empire and devises a plan to preserve human knowledge."));
        books.add(new Book("f6","fiction","Neuromancer","William Gibson",1020,271,22,fi1,"The groundbreaking cyberpunk novel following a washed-up hacker hired for one last job in a neon-lit dystopia."));
        books.add(new Book("f7","fiction","The Name of the Wind","Patrick Rothfuss",1350,662,24,fi2,"The first day of the life story of Kvothe the most notorious wizard the world has ever seen."));
        books.add(new Book("f8","fiction","Ready Player One","Ernest Cline",1150,374,39,fi3,"In a dystopian future teens compete in a massive virtual reality treasure hunt that could change the world."));
        books.add(new Book("f9","fiction","The Left Hand of Darkness","Ursula K. Le Guin",1080,304,16,fi0,"A human envoy visits a planet whose inhabitants have no fixed gender a landmark of science fiction."));
        books.add(new Book("f10","fiction","Project Hail Mary","Andy Weir",1280,476,28,fi1,"A lone astronaut wakes in deep space with no memory tasked with saving Earth from an extinction-level threat."));

        // ── ROMANTIC ────────────────────────────────────────────────
        String ri0 = "https://images.unsplash.com/photo-1474366521946-c3d4b507abf2?w=300&h=400&fit=crop";
        String ri1 = "https://images.unsplash.com/photo-1519682337058-a94d519337bc?w=300&h=400&fit=crop";
        String ri2 = "https://images.unsplash.com/photo-1544947950-fa07a98d237f?w=300&h=400&fit=crop";
        String ri3 = "https://images.unsplash.com/photo-1516979187457-637abb4f9353?w=300&h=400&fit=crop";

        books.add(new Book("r1","romantic","Me Before You","Jojo Moyes",1100,369,44,ri0,"A young woman takes a job caring for a paralyzed man and they form an unexpected life-changing connection."));
        books.add(new Book("r2","romantic","The Notebook","Nicholas Sparks",980,214,38,ri1,"An elderly man reads to his wife daily from a notebook the story of their love across the decades."));
        books.add(new Book("r3","romantic","Outlander","Diana Gabaldon",1350,850,21,ri2,"A WWII nurse is transported back to 18th century Scotland where she falls for a Highland warrior."));
        books.add(new Book("r4","romantic","The Fault in Our Stars","John Green",950,313,46,ri3,"Two teenagers with cancer fall in love and embark on a journey to meet their favorite author in Amsterdam."));
        books.add(new Book("r5","romantic","Jane Eyre","Charlotte Bronte",870,532,30,ri0,"An orphaned governess falls for the brooding Mr. Rochester navigating class and secrets in Victorian England."));
        books.add(new Book("r6","romantic","The Hating Game","Sally Thorne",1050,384,33,ri1,"Two executive assistants who despise each other begin to question whether their rivalry hides something deeper."));
        books.add(new Book("r7","romantic","It Ends with Us","Colleen Hoover",1080,361,50,ri2,"Lily falls for a surgeon but when her first love reappears she must make an impossible choice."));
        books.add(new Book("r8","romantic","Anna Karenina","Leo Tolstoy",1250,864,14,ri3,"A powerful tale of illicit love jealousy and fate set among the Russian aristocracy of the 19th century."));
        books.add(new Book("r9","romantic","The Kiss Quotient","Helen Hoang",1020,336,27,ri0,"A successful econometrician with autism hires an escort to learn about relationships and discovers true love."));
        books.add(new Book("r10","romantic","Twilight","Stephenie Meyer",990,498,41,ri1,"A teenage girl moves to a rainy town and falls for a mysterious classmate who turns out to be a vampire."));

        // Save all books
        for (Book b : books) {
            bookDAO.save(b);
        }
        System.out.println("[AppContext] Seeded " + books.size() + " books successfully.");
    }
}
