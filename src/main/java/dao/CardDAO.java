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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import model.Card;
import model.Product;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.ExcelStyleDateFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

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
        try ( PreparedStatement st = connection.prepareStatement(sql)) {
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
        try ( PreparedStatement st = connection.prepareStatement(sql)) {
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

    public Date doubleToDate(double date) {
        // Create a calendar instance
        Calendar calendar = Calendar.getInstance();
        // Set the time in milliseconds using the double value
        calendar.setTimeInMillis((long) date * 1000L);
        // Get the date from the calendar
        Date date1 = calendar.getTime();
        return date1;
    }

    public void ImportExcel(String path, double sellPrice, String supplier) {
        InputStream inp;
        ProductDAO pd = new ProductDAO();
        CardDAO cd = new CardDAO();
        List<Product> products = pd.getAllProduct();
        try {
            inp = new FileInputStream(path); // format lại tên nhà mạng + price
            HSSFWorkbook wb = new HSSFWorkbook(new POIFSFileSystem(inp));
            Sheet sheet = wb.getSheetAt(0);
            Row row;
            for (int i = 1; i < sheet.getLastRowNum(); i++) {
                row = sheet.getRow(i);
                if (row != null) {
                    Cell seriCell = row.getCell(0);
                    Cell codeCell = row.getCell(1);
                    Cell expirationDateCell = row.getCell(2);

                    if (seriCell != null && codeCell != null
                            && expirationDateCell != null) {
                        
                        String seri = seriCell.getStringCellValue()+"";
                        String code = codeCell.getStringCellValue()+"";
                        String expiration = expirationDateCell.getStringCellValue();
                        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                        Date expirationDate = null;
                        try {
                            expirationDate = formatter.parse(expiration);
                        } catch (ParseException ex) {
                            Logger.getLogger(CardDAO.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        int productId = 0;
                        boolean check = false;
                        for (Product p : products) {
                            if (p.getExpirationDate().equals(expirationDate)
                                    && p.getSellPrice() == sellPrice
                                    && p.getSupplier().equals(supplier)) {
                                productId = p.getId();
                                //set updateAt product
                                pd.updateDateProduct(productId);
                                check = true;
                                break;
                            }
                        }
                        if (!check) {
                            Product p = new Product();
                            p.setSellPrice(sellPrice);
                            p.setSupplier(supplier);
                            p.setDescription("mua the nha mang " + supplier);
                            p.setImage(supplier + "_logo.png");
                            p.setStatus(false);
                            p.setCreatedAt(new java.sql.Date((new Date()).getTime()));//get date now
                            pd.addProduct(p,expirationDate);
                            productId = pd.getLastId();
                            products = pd.getAllProduct();
                        }
                        Card card = new Card();
                        card.setSeri(seri);
                        card.setCode(code);
                        card.setPrice(sellPrice);
                        card.setIsBuy(false);
                        card.setExpirationDate(expirationDate);
                        card.setProductId(productId);
                        cd.InsertData(card,expirationDate);
                        int amount = pd.getAmountById(productId);
                        pd.updateAmountProduct(productId, amount);
                    }
                }
                wb.close();

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // upload file java servlet từ máy

    public static void InsertData(Card card,Date expirationDate) {
        String sql = "insert into swp1.card(Seri,Code,price,isBuy,ExpirationDate,CreatedAt,ProductId) values(?,?,?,?,?,?,?)";
        try {
            Connection cnn = (new DBContext()).connection;
            PreparedStatement ptmt = cnn.prepareStatement(sql);
//            ptmt.setInt(1, card.getId());
            ptmt.setString(1, card.getSeri());
            ptmt.setString(2, card.getCode());
            ptmt.setDouble(3, card.getPrice());
            ptmt.setBoolean(4, false);
            ptmt.setDate(5, new java.sql.Date(expirationDate.getTime()));
            Date createdAt = card.getCreatedAt();
            if (createdAt == null) {
                createdAt = new Date(); // Tạo đối tượng Date mới chứa thời gian hiện tại
            }
            ptmt.setDate(6, new java.sql.Date(createdAt.getTime()));

            ptmt.setInt(7, card.getProductId());
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
