/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/File.java to edit this template
 */
package dao;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import model.Product;
import dao.ProductDAO;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import model.Transaction;
import org.apache.poi.poifs.crypt.dsig.services.TSPTimeStampService;

/**
 *
 * @author PC
 */
public class TransactionDAO extends DBContext {

    public void addTransaction(Product p, String account) {

        String sql = "iNSERT INTO `swp1`.`transaction` ( "
                + "`BuyPrice`, `BuyAmount`, `createdAt`, `ProductId`,`accountId`,`Description`)\n"
                + " VALUES (?,?,?,?,?,?);";
        try ( PreparedStatement st = connection.prepareStatement(sql)) {
            st.setDouble(1, p.getSellPrice());
            st.setInt(2, p.getAmount());
            Date d = new Date();
            java.sql.Date createdAt = new java.sql.Date(d.getTime());
            st.setDate(3, createdAt);
            st.setInt(4, p.getId());
            st.setString(5, account);
            st.setString(6, "The dien thoai " + p.getSupplier());
            st.execute();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public int getLastId() {
        String sql = "select Id from transaction order by id desc limit 1;";
        try ( PreparedStatement st = connection.prepareStatement(sql)) {
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return 0;
    }

    public List<Transaction> getAllByAccount(String account) {
        List<Transaction> list = new ArrayList<>();
        String sql = "select * from transaction where accountId = ?";
        try ( PreparedStatement st = connection.prepareStatement(sql)) {
            st.setString(1, account);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                Transaction t = new Transaction(
                        rs.getInt("Id"),
                        rs.getDouble("BuyPrice"),
                        rs.getInt("BuyAmount"),
                        rs.getDate("createdAt"),
                        rs.getString("Description")
                );
                list.add(t);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return list;
    }

    public Transaction findById(int transactionId) {
        String sql = "select * from transaction where Id = ?";
        try ( PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, transactionId);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                Transaction t = new Transaction(
                        rs.getInt("Id"),
                        rs.getDouble("BuyPrice"),
                        rs.getInt("BuyAmount"),
                        rs.getDate("createdAt"),
                        rs.getString("Description")
                );
                return t;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public List<Transaction> getAllByAccount(String account, int start, int end) {
        List<Transaction> list = new ArrayList<>();
        List<Transaction> list1 = new ArrayList<>();
        TransactionDAO td = new TransactionDAO();
        list = td.getAllByAccount(account);
        for (int i = start; i < end; i++) {
            list1.add(list.get(i));
        }
        return list1;
    }

    public List<Transaction> getAllTransaction() {
        List<Transaction> list = new ArrayList<>();
        String sql = "select * from transaction";
        try ( PreparedStatement st = connection.prepareStatement(sql)) {
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                Transaction t = new Transaction(
                        rs.getInt("Id"),
                        rs.getDouble("BuyPrice"),
                        rs.getInt("BuyAmount"),
                        rs.getDate("createdAt"),
                        rs.getString("Description"),
                        rs.getString("AccountId")
                );
                list.add(t);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return list;
    }

}
