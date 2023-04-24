package queries;

import entities.Book;
import entities.Borrow;

import java.util.List;
import java.text.SimpleDateFormat;

public class BorrowHistories {

    public static class Item {
        private int cardId;
        private int bookId;
        private String category;
        private String title;
        private String press;
        private int publishYear;
        private String author;
        private double price;
        private long borrowTime;
        private long returnTime;

        public Item() {
        }

        public Item(int cardId, Book book, Borrow borrow) {
            this.cardId = cardId;
            this.bookId = book.getBookId();
            this.category = book.getCategory();
            this.title = book.getTitle();
            this.press = book.getPress();
            this.publishYear = book.getPublishYear();
            this.author = book.getAuthor();
            this.price = book.getPrice();
            this.borrowTime = borrow.getBorrowTime();
            this.returnTime = borrow.getReturnTime();
        }

        @Override
        public String toString() {
            return "Item {" + "cardId=" + cardId +
                    ", bookId=" + bookId +
                    ", category='" + category + '\'' +
                    ", title='" + title + '\'' +
                    ", press='" + press + '\'' +
                    ", publishYear=" + publishYear +
                    ", author='" + author + '\'' +
                    ", price=" + price +
                    ", borrowTime=" + borrowTime +
                    ", returnTime=" + returnTime +
                    '}';
        }

        public int getCardId() {
            return cardId;
        }

        public void setCardId(int cardId) {
            this.cardId = cardId;
        }

        public int getBookId() {
            return bookId;
        }

        public void setBookId(int bookId) {
            this.bookId = bookId;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getPress() {
            return press;
        }

        public void setPress(String press) {
            this.press = press;
        }

        public int getPublishYear() {
            return publishYear;
        }

        public void setPublishYear(int publishYear) {
            this.publishYear = publishYear;
        }

        public String getAuthor() {
            return author;
        }

        public void setAuthor(String author) {
            this.author = author;
        }

        public double getPrice() {
            return price;
        }

        public void setPrice(double price) {
            this.price = price;
        }

        public long getBorrowTime() {
            return borrowTime;
        }

        public void setBorrowTime(long borrowTime) {
            this.borrowTime = borrowTime;
        }

        public long getReturnTime() {
            return returnTime;
        }

        public void setReturnTime(long returnTime) {
            this.returnTime = returnTime;
        }


        /*
         * ouput like this:
         * +--------+----------+-----------+---------+------+--------+-------+---------------+---------------+
         * | BookID | Catagory |   Title   |  Press  | Year | Author | Price |   BorrowTime  |   ReturnTime  |
         * +--------+----------+-----------+---------+------+--------+-------+---------------+---------------+
         */
        public void infoOutput() {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            String bookIdString = String.valueOf(bookId);
            String categoryString = category;
            String titleString = title;
            String pressString = press;
            String publishYearString = String.valueOf(publishYear);
            String authorString = author;
            String priceString = String.valueOf(price);
            String borrowTimeString = String.valueOf(sdf.format(borrowTime));
            String returnTimeString = "null";
            if (returnTime != 0) {
                returnTimeString = String.valueOf(sdf.format(returnTime));
            }

            int bookIdLine = (bookIdString.length() - 1) / 6 + 1;
            int categoryLine = (categoryString.length() - 1) / 8 + 1;
            int titleLine = (titleString.length() - 1) / 9 + 1;
            int pressLine = (pressString.length() - 1) / 7 + 1;
            int publishYearLine = (publishYearString.length() - 1) / 4 + 1;
            int authorLine = (authorString.length() - 1) / 6 + 1;
            int priceLine = (priceString.length() - 1) / 5 + 1;
            int borrowTimeLine = (borrowTimeString.length() - 1) / 13 + 1;
            int returnTimeLine = (returnTimeString.length() - 1) / 13 + 1;
            int maxLine = Math.max(Math.max(Math.max(Math.max(Math.max(Math.max(Math.max(bookIdLine, categoryLine), titleLine), pressLine), publishYearLine), authorLine), priceLine), Math.max(borrowTimeLine, returnTimeLine));
            
            String format = "| %6s | %8s | %9s | %7s | %4s | %6s | %5s | %11s | %11s |%n";
            for (int i = 0; i < maxLine; i++) {
                System.out.format(format,
                        i * 6 < bookIdString.length() ? bookIdString.substring(i * 6, Math.min((i + 1) * 6, bookIdString.length())) : "",
                        i * 8 < categoryString.length() ? categoryString.substring(i * 8, Math.min((i + 1) * 8, categoryString.length())) : "",
                        i * 9 < titleString.length() ? titleString.substring(i * 9, Math.min((i + 1) * 9, titleString.length())) : "",
                        i * 7 < pressString.length() ? pressString.substring(i * 7, Math.min((i + 1) * 7, pressString.length())) : "",
                        i * 4 < publishYearString.length() ? publishYearString.substring(i * 4, Math.min((i + 1) * 4, publishYearString.length())) : "",
                        i * 6 < authorString.length() ? authorString.substring(i * 6, Math.min((i + 1) * 6, authorString.length())) : "",
                        i * 5 < priceString.length() ? priceString.substring(i * 5, Math.min((i + 1) * 5, priceString.length())) : "",
                        i * 11 < borrowTimeString.length() ? borrowTimeString.substring(i * 11, Math.min((i + 1) * 11, borrowTimeString.length())) : "",
                        i * 11 < returnTimeString.length() ? returnTimeString.substring(i * 11, Math.min((i + 1) * 11, returnTimeString.length())) : ""
                );
            }
        }
    }

    private int count;
    private List<Item> items;

    public BorrowHistories(List<Item> items) {
        this.count = items.size();
        this.items = items;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }
}
