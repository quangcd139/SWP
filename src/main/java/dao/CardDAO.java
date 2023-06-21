/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/File.java to edit this template
 */
package dao;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import model.Card;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.ExcelStyleDateFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

/**
 *
 * @author PC
 */
public class CardDAO extends DBContext {

    public void updateStatusCard(int productId, int transactionId, double price, int soLuong) {
        String sql = "UPDATE card\n"
                + "SET transactionId = ?, isBuy = 1\n"
                + "WHERE price=? and productId=? and isBuy = 0\n"
                + "limit ?;";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, transactionId);
            st.setDouble(2, price);
            st.setInt(3, productId);
            st.setInt(4, soLuong);
            st.execute();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public List<Card> getCardByTranId(int transactionId) {
        List<Card> list = new ArrayList<>();
        String sql = "select * from card where transactionId=?;";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, transactionId);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                Card c = new Card(rs.getString("Seri"),
                        rs.getString("Code"),
                        rs.getDouble("price"),
                        rs.getDate("ExpirationDate"),
                        rs.getDate("CreatedAt"));
                list.add(c);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return list;
    }

   public static List<Card> ImportExcel(String path) throws IOException {
        List<Card> cards = new ArrayList<Card>();
        InputStream inp;
        try {
            inp = new FileInputStream(path); // format lại tên nhà mạng + price
            HSSFWorkbook wb = new HSSFWorkbook(new POIFSFileSystem(inp));
            Sheet sheet = wb.getSheetAt(0);
            Row row;

            for (int i = 1; i < sheet.getLastRowNum(); i++) {
                row = sheet.getRow(i);
                if (row != null) {
                    Cell idCell = row.getCell(0);
                    Cell seriCell = row.getCell(1);
                    Cell codeCell = row.getCell(2);
                    Cell priceCell = row.getCell(3);
                    Cell expirationDateCell = row.getCell(4);
                    Cell productIdCell = row.getCell(5);

                    if (idCell != null && seriCell != null && codeCell != null
                            && priceCell != null && expirationDateCell != null && productIdCell != null) {

                        int id = (int) idCell.getNumericCellValue();
                        String seri = seriCell.getStringCellValue();
                        String code = codeCell.getStringCellValue();
                        double price = priceCell.getNumericCellValue();
                        Date expirationDate = expirationDateCell.getDateCellValue();
                        int productId = (int) productIdCell.getNumericCellValue();

                        Card card = new Card();
//                        card.setId(id);
                        card.setSeri(seri);
                        card.setCode(code);
                        card.setPrice(price);
                        card.setBuy(false);
                        card.setExpirationDate(expirationDate);
                        card.setTransactionId(0);
                        card.setProductId(productId);

                        cards.add(card);
                    }
                }
                wb.close();

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cards;
    }
    // upload file java servlet từ máy

    public static void InsertData( Card card) {
        String sql = "insert into swp1.card(Seri,Code,price,isBuy,ExpirationDate,CreatedAt,ProductId) values(?,?,?,?,?,?,?)";
        try {
             Connection cnn = (new DBContext()).connection;

            PreparedStatement ptmt = cnn.prepareStatement(sql);
//            ptmt.setInt(1, card.getId());
            ptmt.setString(1, card.getSeri());
            ptmt.setString(2, card.getCode());
            ptmt.setDouble(3, card.getPrice());
            ptmt.setBoolean(4, false);
            ptmt.setDate(5, new java.sql.Date(card.getExpirationDate().getTime()));
            Date createdAt = card.getCreatedAt();
            if (createdAt == null) {
                createdAt = new Date(); // Tạo đối tượng Date mới chứa thời gian hiện tại
            }
            ptmt.setDate(7, new java.sql.Date(createdAt.getTime()));

            ptmt.setInt(8, card.getProductId());
            int kt = ptmt.executeUpdate();
            if (kt != 0) {
                System.out.println("success");
            } else {
                System.out.println("fail");
            }
            ptmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
