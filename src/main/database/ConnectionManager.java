package main.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.GregorianCalendar;

import application.Main;
import main.person.Person;


public class ConnectionManager {
    private String driver = "org.postgresql.Driver";
    private String user; // = "postgres";
    private String password; // = "sa@123";
    private String url = "jdbc:postgresql://"; //127.0.0.1:5432/SocialNetwork";
    private Connection connection;
    //Booleano que controla se é necessário executar o setup do banco de dados
    //TRUE = executa setup = cria as tabelas
    //FALSE = não executa, só conecta
	private Boolean setup;

    public ConnectionManager() { }

    public ConnectionManager(String host, String port, String database, String user, String password, Boolean setup) {

    	StringBuilder builderURL = new StringBuilder();
    	builderURL.append(this.url);
    	builderURL.append(host);
    	builderURL.append(":");
    	builderURL.append(port);
    	builderURL.append("/");
    	builderURL.append(database);

    	setUser(user);
    	setPassword(password);
    	setUrl(builderURL.toString());
    	setSetup(setup);
    }

    public void open() throws SQLException, ClassNotFoundException {
        Class.forName(driver);
        System.out.println("URL: " + url);
        System.out.println("Usuário: " + user);
        System.out.println("Senha: " + password);
        Connection connection = DriverManager.getConnection(url, user, password);
        setConnection(connection);

        if(getSetup() == Boolean.TRUE) {
        	System.out.println("Executando setup do banco de dados...");
        	createTables(getConnection());
        }
    }

    public void close() {
        try {
            getConnection().close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createTables(Connection connection) {
        createTablePerson(connection);
        createTableFriendship(connection);
    }

    private void createTablePerson(Connection connection) {
        try {
            String sql = "CREATE TABLE Person ( "
                    + "codigo SERIAL PRIMARY KEY, "
                    + "nome VARCHAR(255), "
                    + "email VARCHAR(255), "
                    + "dataNascimento DATE, "
                    + "cidadeNascimento VARCHAR(255),"
                    + "cidadeResidencia VARCHAR(255)"
                    + ");";
            Statement statement = connection.createStatement();
            statement.execute(sql);
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createTableFriendship(Connection connection) {
        try {
            String sql = "CREATE TABLE Friendship ( "
                    + "codPessoa int NOT NULL, "
                    + "codAmigo int NOT NULL,"
                    + "dataInicio date, "
                    + "CONSTRAINT PK_pessoa_amigos PRIMARY KEY (codPessoa, codAmigo)"
                    + ");"

                    + "ALTER TABLE Friendship ADD CONSTRAINT FX_codPessoa "
                    + "FOREIGN KEY (codPessoa) REFERENCES Person (codigo); "

                    + "ALTER TABLE Friendship ADD CONSTRAINT FK_codAmigo "
                    + "FOREIGN KEY (codAmigo) REFERENCES Person (codigo);";

            Statement statement = connection.createStatement();
            statement.execute(sql);
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public String testConnection() {
    	Connection connection = getConnection();
        String sql = null;
        String person = null, friendship = null, erro = null;
        try {
        	Statement statement = connection.createStatement();
        	
            sql = "SELECT COUNT(*) AS count FROM Person";
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
            	person = resultSet.getString("count");
            }
            
            sql = "SELECT COUNT(*) AS count FROM Friendship";
            resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
            	friendship = resultSet.getString("count");
            }
            resultSet.close();
            statement.close();
            
            if(person != null || friendship != null) {
            	return "Sucess";
            } else {
            	return "Error";
            }
        } catch (Exception e) {
            e.printStackTrace();
            
            if (e.getMessage().contains("relation \"person\" does not exist")) {
            	erro = "A tabela de pessoas não existe. Execute os scripts de criação.";
            } else if (erro.contains("relation \"friendship\" does not exist")) {
            	erro = "A tabela de relação entre pessoas e amigos não existe. Execute os scripts de criação.";
            } else {
            	erro = e.getMessage();
            }
            return erro;
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Boolean getSetup() {
		return setup;
	}

	public void setSetup(Boolean setup) {
		this.setup = setup;
	}
}
