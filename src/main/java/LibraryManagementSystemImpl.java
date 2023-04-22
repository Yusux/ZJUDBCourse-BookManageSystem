import entities.Book;
import entities.Borrow;
import entities.Card;
import entities.Card.CardType;
import queries.*;
import queries.BorrowHistories.Item;
import utils.DBInitializer;
import utils.DatabaseConnector;

import java.sql.*;
import java.util.List;

import java.util.ArrayList;

public class LibraryManagementSystemImpl implements LibraryManagementSystem {

    private final DatabaseConnector connector;

    public LibraryManagementSystemImpl(DatabaseConnector connector) {
        this.connector = connector;
    }

    @Override
    public ApiResult storeBook(Book book) {
        Connection conn = connector.getConn();
        try {
            String sql = "insert into book (book_id, category, title, press, publish_year, author, price, stock) values (0, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, book.getCategory());
            stmt.setString(2, book.getTitle());
            stmt.setString(3, book.getPress());
            stmt.setInt(4, book.getPublishYear());
            stmt.setString(5, book.getAuthor());
            stmt.setDouble(6, book.getPrice());
            stmt.setInt(7, book.getStock());
            stmt.executeUpdate();
            String query = "select book_id from book where category = ? and title = ? and press = ? and publish_year = ? and author = ? and price = ? and stock = ?";
            PreparedStatement queryStmt = conn.prepareStatement(query);
            queryStmt.setString(1, book.getCategory());
            queryStmt.setString(2, book.getTitle());
            queryStmt.setString(3, book.getPress());
            queryStmt.setInt(4, book.getPublishYear());
            queryStmt.setString(5, book.getAuthor());
            queryStmt.setDouble(6, book.getPrice());
            queryStmt.setInt(7, book.getStock());
            ResultSet rs = queryStmt.executeQuery();
            if (rs.next()) {
                book.setBookId(rs.getInt(1));
            }
            commit(conn);
        } catch (Exception e) {
            rollback(conn);
            // System.err.println(e.getMessage());
            return new ApiResult(false, e.getMessage());
        }
        return new ApiResult(true, null);
    }

    @Override
    public ApiResult incBookStock(int bookId, int deltaStock) {
        Connection conn = connector.getConn();
        try {
            String querysql = "select stock from book where book_id = ? for update";
            PreparedStatement queryStmt = conn.prepareStatement(querysql);
            queryStmt.setInt(1, bookId);
            ResultSet rs = queryStmt.executeQuery();
            if (!rs.next()) {
                throw new Exception("Book not found");
            }
            int stock = rs.getInt(1);
            if (stock + deltaStock < 0) {
                throw new Exception("Stock not enough");
            }
            String sql = "update book set stock = ? where book_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, stock + deltaStock);
            stmt.setInt(2, bookId);
            stmt.executeUpdate();
            commit(conn);
        } catch (Exception e) {
            rollback(conn);
            // System.err.println(e.getMessage());
            return new ApiResult(false, e.getMessage());
        }
        return new ApiResult(true, null);
    }

    @Override
    public ApiResult storeBook(List<Book> books) {
        Connection conn = connector.getConn();
        try {
            String sql = "insert into book (book_id, category, title, press, publish_year, author, price, stock) values (0, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            String query = "select book_id from book where category = ? and title = ? and press = ? and publish_year = ? and author = ? and price = ? and stock = ?";
            PreparedStatement queryStmt = conn.prepareStatement(query);
            for (Book book : books) {
                stmt.setString(1, book.getCategory());
                stmt.setString(2, book.getTitle());
                stmt.setString(3, book.getPress());
                stmt.setInt(4, book.getPublishYear());
                stmt.setString(5, book.getAuthor());
                stmt.setDouble(6, book.getPrice());
                stmt.setInt(7, book.getStock());
                stmt.executeUpdate();
                queryStmt.setString(1, book.getCategory());
                queryStmt.setString(2, book.getTitle());
                queryStmt.setString(3, book.getPress());
                queryStmt.setInt(4, book.getPublishYear());
                queryStmt.setString(5, book.getAuthor());
                queryStmt.setDouble(6, book.getPrice());
                queryStmt.setInt(7, book.getStock());
                ResultSet rs = queryStmt.executeQuery();
                if (rs.next()) {
                    book.setBookId(rs.getInt(1));
                }
            }
            commit(conn);
        } catch (Exception e) {
            rollback(conn);
            // System.err.println(e.getMessage());
            return new ApiResult(false, e.getMessage());
        }
        return new ApiResult(true, null);
    }

    @Override
    public ApiResult removeBook(int bookId) {
        Connection conn = connector.getConn();
        try {
            // check book_id
            String querysql = "select book_id from book where book_id = ?";
            PreparedStatement queryStmt = conn.prepareStatement(querysql);
            queryStmt.setInt(1, bookId);
            ResultSet rs = queryStmt.executeQuery();
            if (!rs.next()) {
                throw new Exception("Book not found");
            }

            // check if unreturned book exists
            querysql = "select book_id from borrow where book_id = ? and return_time = 0 for update";
            queryStmt = conn.prepareStatement(querysql);
            queryStmt.setInt(1, bookId);
            rs = queryStmt.executeQuery();
            if (rs.next()) {
                throw new Exception("Unreturned book exists");
            }

            // remove book
            String sql = "delete from book where book_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, bookId);
            stmt.executeUpdate();

            commit(conn);
        } catch (Exception e) {
            rollback(conn);
            // System.err.println(e.getMessage());
            return new ApiResult(false, e.getMessage());
        }
        return new ApiResult(true, null);
    }

    @Override
    public ApiResult modifyBookInfo(Book book) {
        Connection conn = connector.getConn();
        try {
            // check book_id
            String querysql = "select book_id from book where book_id = ? for update";
            PreparedStatement queryStmt = conn.prepareStatement(querysql);
            queryStmt.setInt(1, book.getBookId());
            ResultSet rs = queryStmt.executeQuery();
            if (!rs.next()) {
                throw new Exception("Book not found");
            }

            // check new price
            if (book.getPrice() < 0) {
                throw new Exception("Price must be positive");
            }

            String sql = "update book set category = ?, title = ?, press = ?, publish_year = ?, author = ?, price = ? where book_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, book.getCategory());
            stmt.setString(2, book.getTitle());
            stmt.setString(3, book.getPress());
            stmt.setInt(4, book.getPublishYear());
            stmt.setString(5, book.getAuthor());
            stmt.setDouble(6, book.getPrice());
            stmt.setInt(7, book.getBookId());
            stmt.executeUpdate();
            commit(conn);
        } catch (Exception e) {
            rollback(conn);
            // System.err.println(e.getMessage());
            return new ApiResult(false, e.getMessage());
        }
        return new ApiResult(true, null);
    }

    @Override
    public ApiResult queryBook(BookQueryConditions conditions) {
        Connection conn = connector.getConn();
        try {
            String query = "select * from book where";

            if (conditions.getCategory() != null) {
                query += " category = ? and";
            }
            if (conditions.getTitle() != null) {
                query += " title like ? and";
            }
            if (conditions.getPress() != null) {
                query += " press like ? and";
            }
            if (conditions.getMinPublishYear() != null) {
                query += " publish_year >= ? and";
            }
            if (conditions.getMaxPublishYear() != null) {
                query += " publish_year <= ? and";
            }
            if (conditions.getAuthor() != null) {
                query += " author like ? and";
            }
            if (conditions.getMinPrice() != null) {
                query += " price >= ? and";
            }
            if (conditions.getMaxPrice() != null) {
                query += " price <= ? and";
            }
            query = query.substring(0, query.length() - 4);
            
            if (conditions.getSortBy() != null) {
                query += " order by " + conditions.getSortBy().toString();
                if (conditions.getSortOrder() != null) {
                    query += " " + conditions.getSortOrder().toString();
                }
            }
            if (conditions.getSortBy() != Book.SortColumn.BOOK_ID) {
                query += ", book_id asc";
            }
            // System.out.println(query);
        
            PreparedStatement queryStmt = conn.prepareStatement(query);
            int index = 1;
            if (conditions.getCategory() != null) {
                queryStmt.setString(index++, conditions.getCategory());
            }
            if (conditions.getTitle() != null) {
                queryStmt.setString(index++, "%" + conditions.getTitle() + "%");
            }
            if (conditions.getPress() != null) {
                queryStmt.setString(index++, "%" + conditions.getPress() + "%");
            }
            if (conditions.getMinPublishYear() != null) {
                queryStmt.setInt(index++, conditions.getMinPublishYear());
            }
            if (conditions.getMaxPublishYear() != null) {
                queryStmt.setInt(index++, conditions.getMaxPublishYear());
            }
            if (conditions.getAuthor() != null) {
                queryStmt.setString(index++, "%" + conditions.getAuthor() + "%");
            }
            if (conditions.getMinPrice() != null) {
                queryStmt.setDouble(index++, conditions.getMinPrice());
            }
            if (conditions.getMaxPrice() != null) {
                queryStmt.setDouble(index++, conditions.getMaxPrice());
            }
            ResultSet rs = queryStmt.executeQuery();

            List <Book>books=new ArrayList<>();
            while (rs.next()) {
                Book book=new Book();
                book.setBookId(rs.getInt(1));
                book.setCategory(rs.getString(2));
                book.setTitle(rs.getString(3));
                book.setPress(rs.getString(4));
                book.setPublishYear(rs.getInt(5));
                book.setAuthor(rs.getString(6));
                book.setPrice(rs.getDouble(7));
                book.setStock(rs.getInt(8));
                books.add(book);
            }
            commit(conn);
            return new ApiResult(true, null, new BookQueryResults(books));
        } catch (Exception e) {
            rollback(conn);
            // System.err.println(e.getMessage());
            return new ApiResult(false, e.getMessage());
        }
    }

    @Override
    public ApiResult borrowBook(Borrow borrow) {
        Connection conn = connector.getConn();
        try {
            // check book_id and stock
            String querysql = "select stock from book where book_id = ? for update";
            PreparedStatement queryStmt = conn.prepareStatement(querysql);
            queryStmt.setInt(1, borrow.getBookId());
            ResultSet rs = queryStmt.executeQuery();
            if (!rs.next()) {
                throw new Exception("Book not found");
            }
            int stock = rs.getInt(1);
            if (stock <= 0) {
                throw new Exception("Book out of stock");
            }

            // check card_id
            querysql = "select * from card where card_id = ? for update";
            queryStmt = conn.prepareStatement(querysql);
            queryStmt.setInt(1, borrow.getCardId());
            rs = queryStmt.executeQuery();
            if (!rs.next()) {
                throw new Exception("Card not found");
            }

            // check borrow history
            querysql = "select * from borrow where card_id = ? and book_id = ? and return_time = 0 for update";
            queryStmt = conn.prepareStatement(querysql);
            queryStmt.setInt(1, borrow.getCardId());
            queryStmt.setInt(2, borrow.getBookId());
            rs = queryStmt.executeQuery();
            if (rs.next()) {
                throw new Exception("Book already borrowed");
            }

            // insert borrow
            String insertsql = "insert into borrow (card_id, book_id, borrow_time) values (?, ?, ?)";
            PreparedStatement insertStmt = conn.prepareStatement(insertsql);
            insertStmt.setInt(1, borrow.getCardId());
            insertStmt.setInt(2, borrow.getBookId());
            insertStmt.setLong(3, borrow.getBorrowTime());
            insertStmt.executeUpdate();

            // update stock
            String updatesql = "update book set stock = ? where book_id = ?";
            PreparedStatement updateStmt = conn.prepareStatement(updatesql);
            updateStmt.setInt(1, stock - 1);
            updateStmt.setInt(2, borrow.getBookId());
            updateStmt.executeUpdate();
            commit(conn);
        } catch (Exception e) {
            rollback(conn);
            // System.err.println(e.getMessage());
            return new ApiResult(false, e.getMessage());
        }
        return new ApiResult(true, null);
    }

    @Override
    public ApiResult returnBook(Borrow borrow) {
        Connection conn = connector.getConn();
        try {
            // check book_id and card_id
            String querysql = "select * from borrow where card_id = ? and book_id = ? and borrow_time = ? and return_time = 0 for update";
            PreparedStatement queryStmt = conn.prepareStatement(querysql);
            queryStmt.setInt(1, borrow.getCardId());
            queryStmt.setInt(2, borrow.getBookId());
            queryStmt.setLong(3, borrow.getBorrowTime());
            ResultSet rs = queryStmt.executeQuery();
            if (!rs.next()) {
                throw new Exception("Book borrow history not found");
            }
            if (borrow.getReturnTime() <= rs.getLong(3)) {
                throw new Exception("Return time cannot be earlier than or equal to borrow time");
            }
            
            // update borrow
            String updatesql = "update borrow set return_time = ? where card_id = ? and book_id = ? and return_time = 0";
            PreparedStatement updateStmt = conn.prepareStatement(updatesql);
            updateStmt.setLong(1, borrow.getReturnTime());
            updateStmt.setInt(2, borrow.getCardId());
            updateStmt.setInt(3, borrow.getBookId());
            updateStmt.executeUpdate();

            // update stock
            querysql = "select stock from book where book_id = ? for update";
            queryStmt = conn.prepareStatement(querysql);
            queryStmt.setInt(1, borrow.getBookId());
            rs = queryStmt.executeQuery();
            if (!rs.next()) {
                throw new Exception("Book not found");
            }
            int stock = rs.getInt(1);
            updatesql = "update book set stock = ? where book_id = ?";
            updateStmt = conn.prepareStatement(updatesql);
            updateStmt.setInt(1, stock + 1);
            updateStmt.setInt(2, borrow.getBookId());
            updateStmt.executeUpdate();

            commit(conn);
        } catch (Exception e) {
            rollback(conn);
            // System.err.println(e.getMessage());
            return new ApiResult(false, e.getMessage());
        }
        return new ApiResult(true, null);
    }

    @Override
    public ApiResult showBorrowHistory(int cardId) {
        Connection conn = connector.getConn();
        try {
            // check card_id
            String querysql = "select * from card where card_id = ?";
            PreparedStatement queryStmt = conn.prepareStatement(querysql);
            queryStmt.setInt(1, cardId);
            ResultSet rs = queryStmt.executeQuery();
            if (!rs.next()) {
                throw new Exception("Card not found");
            }

            // query borrow history
            querysql = "select * from borrow where card_id = ? order by borrow_time desc, book_id asc";
            queryStmt = conn.prepareStatement(querysql);
            queryStmt.setInt(1, cardId);
            rs = queryStmt.executeQuery();
            querysql = "select * from book where book_id = ?";
            queryStmt = conn.prepareStatement(querysql);
            List<Item> items = new ArrayList<>();
            while (rs.next()) {
                Borrow borrow = new Borrow(rs.getInt(2), rs.getInt(1));
                borrow.setBorrowTime(rs.getLong(3));
                borrow.setReturnTime(rs.getLong(4));
                queryStmt.setInt(1, borrow.getBookId());
                ResultSet rs2 = queryStmt.executeQuery();
                if (!rs2.next()) {
                    throw new Exception("Book not found");
                }
                Book book = new Book();
                book.setBookId(rs2.getInt(1));
                book.setCategory(rs2.getString(2));
                book.setTitle(rs2.getString(3));
                book.setPress(rs2.getString(4));
                book.setPublishYear(rs2.getInt(5));
                book.setAuthor(rs2.getString(6));
                book.setPrice(rs2.getDouble(7));
                book.setStock(rs2.getInt(8));
                items.add(new Item(cardId, book, borrow));
            }
            BorrowHistories histories = new BorrowHistories(items);
            commit(conn);
            return new ApiResult(true, null, histories);
        } catch (Exception e) {
            rollback(conn);
            // System.err.println(e.getMessage());
            return new ApiResult(false, e.getMessage());
        }
    }

    @Override
    public ApiResult registerCard(Card card) {
        Connection conn = connector.getConn();
        try {
            String sql = "insert into card (card_id, name, department, type) values (0, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, card.getName());
            stmt.setString(2, card.getDepartment());
            stmt.setString(3, card.getType().getStr());
            stmt.executeUpdate();
            String querysql = "select card_id from card where name = ? and department = ? and type = ?";
            PreparedStatement queryStmt = conn.prepareStatement(querysql);
            queryStmt.setString(1, card.getName());
            queryStmt.setString(2, card.getDepartment());
            queryStmt.setString(3, card.getType().getStr());
            ResultSet rs = queryStmt.executeQuery();
            if (!rs.next()) {
                throw new Exception("Card_id Increment Error");
            }
            card.setCardId(rs.getInt(1));
            commit(conn);
        } catch (Exception e) {
            rollback(conn);
            // System.err.println(e.getMessage());
            return new ApiResult(false, e.getMessage());
        }
        return new ApiResult(true, null);
    }

    @Override
    public ApiResult removeCard(int cardId) {
        Connection conn = connector.getConn();
        try {
            // check card_id
            String sql = "select * from card where card_id = ? for update";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, cardId);
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) {
                throw new Exception("Card not found");
            }

            // check borrow
            sql = "select * from borrow where card_id = ? and return_time = 0";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, cardId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                throw new Exception("Card has not returned all books");
            }

            // delete card
            sql = "delete from card where card_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, cardId);
            stmt.executeUpdate();

            commit(conn);
        } catch (Exception e) {
            rollback(conn);
            // System.err.println(e.getMessage());
            return new ApiResult(false, e.getMessage());
        }
        return new ApiResult(true, null);
    }

    @Override
    public ApiResult showCards() {
        Connection conn = connector.getConn();
        try {
            String sql = "select * from card order by card_id asc";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            List<Card> cards = new ArrayList<>();
            while (rs.next()) {
                Card card = new Card();
                card.setCardId(rs.getInt(1));
                card.setName(rs.getString(2));
                card.setDepartment(rs.getString(3));
                card.setType(CardType.values(rs.getString(4)));
                cards.add(card);
            }
            CardList cardList = new CardList(cards);
            commit(conn);
            return new ApiResult(true, null, cardList);
        } catch (Exception e) {
            rollback(conn);
            // System.err.println(e.getMessage());
            return new ApiResult(false, e.getMessage());
        }
    }

    @Override
    public ApiResult resetDatabase() {
        Connection conn = connector.getConn();
        try {
            Statement stmt = conn.createStatement();
            DBInitializer initializer = connector.getConf().getType().getDbInitializer();
            stmt.addBatch(initializer.sqlDropBorrow());
            stmt.addBatch(initializer.sqlDropBook());
            stmt.addBatch(initializer.sqlDropCard());
            stmt.addBatch(initializer.sqlCreateCard());
            stmt.addBatch(initializer.sqlCreateBook());
            stmt.addBatch(initializer.sqlCreateBorrow());
            stmt.executeBatch();
            commit(conn);
        } catch (Exception e) {
            rollback(conn);
            return new ApiResult(false, e.getMessage());
        }
        return new ApiResult(true, null);
    }

    private void rollback(Connection conn) {
        try {
            conn.rollback();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void commit(Connection conn) {
        try {
            conn.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
