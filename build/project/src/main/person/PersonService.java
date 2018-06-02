package main.person;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import application.Main;


public class PersonService {

	public static PersonService instance = new PersonService();

	public List<Person> getTodasAsPessoasDoSistema() {
		List<Person> pessoas = new ArrayList<>();

        Connection connection =  Main.connectionManager.getConnection();
        String sql = null;
        try {
            sql = "SELECT codigo, nome, email, dataNascimento, cidadeNascimento, cidadeResidencia FROM Person"
            		+ " ORDER BY codigo ASC";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                Person pessoa = new Person();
                pessoa.setCodigo(resultSet.getLong("codigo"));
                pessoa.setNome(resultSet.getString("nome"));
                pessoa.setEmail(resultSet.getString("email"));

                GregorianCalendar gc = new GregorianCalendar();
                gc.setTime(resultSet.getDate("dataNascimento"));
                pessoa.setDataNascimento(gc);

                pessoa.setCidadeNascimento(resultSet.getString("cidadeNascimento"));
                pessoa.setCidadeResidencia(resultSet.getString("cidadeResidencia"));
                
                pessoa.setAmigos(getAmigos(pessoa.getCodigo().toString()));
                pessoas.add(pessoa);
            }
            resultSet.close();
            statement.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return pessoas;
    }


    public Person getPessoa(String codigo) {
        Person pessoa = null;

        Connection connection =  Main.connectionManager.getConnection();
        String sql = null;
        try {
            sql = "SELECT codigo, nome, email, dataNascimento, cidadeNascimento, cidadeResidencia "
                    + "FROM Person WHERE codigo = " + codigo;
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                pessoa = new Person();
                pessoa.setCodigo(resultSet.getLong("codigo"));
                pessoa.setNome(resultSet.getString("nome"));
                pessoa.setEmail(resultSet.getString("email"));

                GregorianCalendar gc = new GregorianCalendar();
                gc.setTime(resultSet.getDate("dataNascimento"));
                pessoa.setDataNascimento(gc);

                pessoa.setCidadeNascimento(resultSet.getString("cidadeNascimento"));
                pessoa.setCidadeResidencia(resultSet.getString("cidadeResidencia"));
                pessoa.setAmigos(getAmigos(codigo));
            }
            resultSet.close();
            statement.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return pessoa;
    }

    public List<Person> getAmigos(String codigo) {
        List<Person> amigos = new ArrayList<>();

        Connection connection =  Main.connectionManager.getConnection();
        String sql = null;
        try {
            sql = "SELECT codigo, nome, email, dataNascimento, cidadeNascimento, cidadeResidencia "
                    + "FROM Person WHERE codigo IN "
                    + "(SELECT codAmigo FROM Friendship WHERE codPessoa = " + codigo + ")";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                Person pessoa = new Person();
                pessoa.setCodigo(resultSet.getLong("codigo"));
                pessoa.setNome(resultSet.getString("nome"));
                pessoa.setEmail(resultSet.getString("email"));

                GregorianCalendar gc = new GregorianCalendar();
                gc.setTime(resultSet.getDate("dataNascimento"));
                pessoa.setDataNascimento(gc);

                pessoa.setCidadeNascimento(resultSet.getString("cidadeNascimento"));
                pessoa.setCidadeResidencia(resultSet.getString("cidadeResidencia"));

                amigos.add(pessoa);
            }
            resultSet.close();
            statement.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return amigos;
    }

    public void insertPerson(Person pessoa) throws Exception {
        Connection connection =  Main.connectionManager.getConnection();
        String sql = null;
        try {
            sql = "INSERT INTO Person (nome, email, dataNascimento, cidadeNascimento, cidadeResidencia) "
                    + "VALUES (?, ?, ?, ?, ?)";

            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, pessoa.getNome());
            stmt.setString(2, pessoa.getEmail());
            // java.util.Date cannot be cast to java.sql.Date
            stmt.setDate(3, new java.sql.Date(pessoa.getDataNascimento().getTime().getTime()));
            stmt.setString(4, pessoa.getCidadeNascimento());
            stmt.setString(5, pessoa.getCidadeResidencia());
            stmt.execute();
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updatePerson(Person pessoa) throws Exception {
        Connection connection =  Main.connectionManager.getConnection();
        String sql = null;
    	sql = "UPDATE Person SET nome = ?, email = ?, dataNascimento = ?, cidadeNascimento = ?, cidadeResidencia = ? "
        		+ "WHERE codigo = ?";
    	PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setString(1, pessoa.getNome());
        stmt.setString(2, pessoa.getEmail());
        stmt.setDate(3, new java.sql.Date(pessoa.getDataNascimento().getTime().getTime()));
        stmt.setString(4, pessoa.getCidadeNascimento());
        stmt.setString(5, pessoa.getCidadeResidencia());
        stmt.setLong(6, pessoa.getCodigo());
        
        deleteAllFriendsOfThePerson(pessoa);
        
        for(Person amigo : pessoa.getAmigos()) {
        	insertFriend(pessoa, amigo);
        }
        
        stmt.execute();
        stmt.close();
    }
    
    public void deletePessoa(Person pessoa) {
        Connection connection =  Main.connectionManager.getConnection();
        String sql = null;
        try {
            Statement statement = connection.createStatement();

            sql = "DELETE FROM Friendship WHERE codPessoa = " + pessoa.getCodigo();
            statement.execute(sql);

            sql = "DELETE FROM Person WHERE codigo = " + pessoa.getCodigo();
            statement.execute(sql);

            statement.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void insertFriend(Person pessoa, Person amigo) throws Exception {
    	Connection connection =  Main.connectionManager.getConnection();
        String sql = null;

        sql = "INSERT INTO friendship (codPessoa, codAmigo) VALUES (?, ?)";
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setLong(1, pessoa.getCodigo());
        stmt.setLong(2, amigo.getCodigo());
        stmt.execute();
        stmt.close();
    }
    
    public void deleteFriend(Person pessoa, Person amigo) {
    	Connection connection =  Main.connectionManager.getConnection();
        String sql = null;
        try {
            Statement statement = connection.createStatement();

            sql = "DELETE FROM friendship WHERE codPessoa = ? AND codAmigo = ?";
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setLong(1, pessoa.getCodigo());
            stmt.setLong(2, amigo.getCodigo());
            stmt.execute();
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void deleteAllFriendsOfThePerson(Person pessoa) {
    	Connection connection =  Main.connectionManager.getConnection();
        String sql = null;
        try {
            sql = "DELETE FROM friendship WHERE codPessoa = " + pessoa.getCodigo().toString();
            Statement statement = connection.createStatement();
            statement.execute(sql);
            statement.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
