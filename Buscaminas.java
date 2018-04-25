import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

/* ACLARACIONES SOBRE EL CÓDIGO AÑADIDO
Las implementaciones añadidas irán comentadas mediante un comentario multilinea, 
poniendo una linea antes la explicación de dicho código y una linea sin comentar 
al finalizar */

/**
 * Clase principal
 * @author Luis Herrera Collado
 *
 */
public class Buscaminas extends JFrame implements Runnable, ActionListener, MouseListener, Serializable
{
	private static final long serialVersionUID = 1L;

	
    
    /* Añadimos las variables que vamos a necesitar para las mejoras */
    int rem_mines, milesimas = 0, segundos = 0, seleccionado, seleccionado2, aux, minas, filas, columnas; 
    private JMenuBar menu; // Variable para el menú
    private JMenu m_juego, m_ayuda; // Menú para la pestaña 'juego' y 'ayuda'
    private JMenuItem m_nuevo, m_stats, m_opc, m_salir, m_guardar, m_cargar; // Items del menú juego
    private JFrame f_stats, f_opc; // Ventanas para las estadísticas y para las opciones
    private JRadioButton b_principiante, b_intermedio, b_experto, b_pers; // Botones para elegir la dificultad del juego
    private ButtonGroup grupo; // Grupo de botones para los radiobutton
    private JPanel panel_botones, panel_pers; // Panel que contendrá los botones radiobutton y panel para personalizado
    private JTextArea e_prin, e_inter, e_exp, e_est; // Etiquetas para los distintos tipos de juego
    private JLabel l_filas, l_columnas, l_minas, l_rem_mines, t_rem_mines, l_time, t_time;
    private JTextField t_filas, t_columnas, t_minas;
    private JButton b_aceptar, b_cancelar;
    private final String ruta_guardar = "Temp/Partidas", ruta_estadisticas = "Temp/Estadisticas"; // Rutas donde se guardaran las estadisticas y las partidas
    private String nombre_jugador;
    private Thread proceso_cronometro; // Hilo para correr el contador de segundos
    private Boolean crono_activo, prin = false, inter = false, exp = false, pers = false; // Booleanos para saber en que modo de juego estamos
    private String[] opciones ={"Empezar de nuevo", "Salir"};
    private String[] opciones2 = {"Sí","No"};
    private String[] opciones_est = {"Principiante","Intermedio","Experto","Personalizado"};
    /**/ 
    
    int nomines; // Número de minas
    int perm[][];
    String tmp; // Variable auxiliar
    boolean found = false;
    int row; // Filas
    int column; // Columnas
    int guesses[][];
    JButton b[][];
    int[][] mines;
    boolean allmines;
    int n; // Filas
    int m; // Columnas
    int deltax[] = {-1, 0, 1, -1, 1, -1, 0, 1};
    int deltay[] = {-1, -1, -1, 0, 0, 1, 1, 1};
    double starttime;
    double endtime;
    
    public Buscaminas(int mi, int fi, int col)
    {
    	
    	/* Inicializamos las minas, las filas, las columnas y las minas restantes pasadas como parámetros */
    	this.nomines = mi;
    	this.n = fi;
    	this.m = col;
    	this.rem_mines = nomines;
    	/**/
    	
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        
        /* Inicialización del menú */
        menu = new JMenuBar();
        this.setJMenuBar(menu);
        
        m_juego = new JMenu("Juego"); // Menú Juego
        m_juego.setMnemonic(KeyEvent.VK_J);
        menu.add(m_juego);
        
        m_nuevo = new JMenuItem("Nuevo juego"); // Submenú nuevo juego
        m_nuevo.setMnemonic(KeyEvent.VK_N);
		m_nuevo.addActionListener(this);
        m_juego.add(m_nuevo);
        m_juego.addSeparator();
        
        m_stats = new JMenuItem("Estadisticas"); // submenú estadísticas
        m_stats.setMnemonic(KeyEvent.VK_E);
        m_stats.addActionListener(this);
        m_juego.add(m_stats);
        
        m_opc = new JMenuItem("Opciones"); // Submenú opciones
        m_opc.setMnemonic(KeyEvent.VK_O);
        m_opc.addActionListener(this);
        m_juego.add(m_opc);
        m_juego.addSeparator();
        
        m_guardar = new JMenuItem("Guardar"); // Submenú guardar
        m_guardar.setMnemonic(KeyEvent.VK_G);
        m_guardar.addActionListener(this);
        m_juego.add(m_guardar);
        
        m_cargar = new JMenuItem("Cargar"); // Submenú cargar
        m_cargar.setMnemonic(KeyEvent.VK_C);
        m_cargar.addActionListener(this);
        m_juego.add(m_cargar);
        m_juego.addSeparator();
        
        m_salir = new JMenuItem("Salir"); // Submenú salir
        m_salir.setMnemonic(KeyEvent.VK_S);
        m_salir.addActionListener(this);
        m_juego.add(m_salir);
        
        m_ayuda = new JMenu("Ayuda"); // Menú ayuda (Por implementar)
        m_ayuda.setMnemonic(KeyEvent.VK_A);
        menu.add(m_ayuda);
        /**/
        
        perm = new int[n][m]; // Creamos una matriz con n filas y m columnas para superponer después la matriz de botones
        boolean allmines = false;
        guesses = new int [n+2][m+2];
        mines = new int[n+2][m+2];
        b = new JButton [n][m];
        
        /* Añadimos una fila más para poner el tiempo y las minas restantes */
        setLayout(new GridLayout(n+1,m));
        /**/
        
        for (int y = 0;y<m+2;y++)
        {
            mines[0][y] = 3;
            mines[n+1][y] = 3;
            guesses[0][y] = 3;
            guesses[n+1][y] = 3;
        } 
        for (int x = 0;x<n+2;x++)
        {
            mines[x][0] = 3;
            mines[x][m+1] = 3;
            guesses[x][0] = 3;
            guesses[x][m+1] = 3;
        }
        do // Se colocan las minas aleatoriamente
        {
            int check = 0; 
            for (int y = 1;y<m+1;y++)
            {
                for (int x = 1;x<n+1;x++)
                {
                    mines[x][y] = 0;
                    guesses[x][y] = 0;
                }
            }
            for (int x = 0;x<nomines;x++)
            {
                mines [(int) (Math.random()*(n)+1)][(int) (Math.random()*(m)+1)] = 1;
            }
            for (int x = 0;x<n;x++)
            {
                for (int y = 0;y<m;y++)
                {
	                if (mines[x+1][y+1] == 1)
	                {
	                        check++;
	                }
                }
            }
            if (check == nomines)
            {
                allmines = true;
            }
        } while (allmines == false);
        
        for (int y = 0;y<m;y++)
        {
            for (int x = 0;x<n;x++)
            {
                if ((mines[x+1][y+1] == 0) || (mines[x+1][y+1] == 1))
                {
                    perm[x][y] = perimcheck(x,y);
                }
                b[x][y] = new JButton("?"); // Inicializamos el texto de cada boton de la matriz
                
                /* Cambiamos los botones para que sean azules */
                b[x][y].setForeground(Color.BLUE);
                b[x][y].setBackground(Color.BLUE);
                /**/
                
                b[x][y].addActionListener(this);
                b[x][y].addMouseListener(this);
                add(b[x][y]);
                b[x][y].setEnabled(true);
            }
        }
        
        /* Texto con las minas restantes y con el tiempo */
        this.t_rem_mines = new JLabel();
        t_rem_mines.setText(String.valueOf(this.rem_mines));
        t_rem_mines.setOpaque(false);
        l_rem_mines = new JLabel();
        l_rem_mines.setText("Minas  ");
        l_rem_mines.setOpaque(false);
        
        this.l_time = new JLabel();
        l_time.setText("Tiempo  ");
        l_time.setOpaque(false);
        this.t_time = new JLabel();
        this.t_time.setText(String.valueOf(0));
        
        this.add(l_rem_mines);
        this.add(t_rem_mines);
        this.add(l_time);
        this.add(t_time);
        /**/
        
        pack();
        setVisible(true);
        
        /*  Iniciamos el cronometro */
        this.iniciar_crono(); 
        /**/
        
        /* Colocamos el frame principal en medio de la pantalla, le ponemos título y evitamos que se pueda agrandar*/
        this.setTitle("Buscaminas AIS");
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        /**/
        
        for (int y = 0;y<m+2;y++)
        {
            for (int x = 0;x<n+2;x++)
            {
                System.out.print(mines[x][y]);
            }
        System.out.println("");}
        starttime = System.nanoTime();
    }
    
    /* Implementacion del metodo run del hilo del cronometro */
    @Override
	public void run() {
    	try
		{
			while (crono_activo)
			{
				Thread.sleep(4);
				milesimas += 4;
				if (milesimas == 1000)
				{
					milesimas = 0;
					segundos++;
					t_time.setText(String.valueOf(segundos));
				}
			}
		}
		catch (Exception e)
		{}
	}
    /**/
    
    /* Metodos para iniciar y detener el cronometro de la partida */
    private void iniciar_crono() 
    {
        this.crono_activo = true;
    	this.proceso_cronometro = new Thread(this);
    	this.proceso_cronometro.start();
	}
    
    private void detener_crono()
    {
    	this.crono_activo = false;
    	this.proceso_cronometro.interrupt();
    }
    /**/
    
	/* Método para habilitar el frame principal */
    public void cerrando_ventana()
    {
    	this.iniciar_crono();
    	this.setEnabled(true);
    	this.setVisible(true);
    }
    /**/
    
    /*
     * Método para ejecutar acciones cuando el usuario pulsa algo en la pantalla principal
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
    	/* Comprobamos qué acción está llevando a cabo el usuario */
    	if (e.getSource() == m_nuevo) // Si pulsa el submenu nuevo se abre una nueva partida con la última configuración elegida por el usuario
    	{
    		minas = this.nomines;
    		filas = this.n;
    		columnas = this.m;
    		this.dispose();
    		new Buscaminas(minas,filas,columnas);
    	}
    	else if (e.getSource() == m_stats) // Si pulsa el submenu estadísticas se abre una ventana con opciones para elegir el nivel que se quiere consultar
    	{
    		this.detener_crono(); // Paramos el cronometro mientras estemos aqui
    		this.f_stats = new JFrame("Estadísticas");
    		f_stats.setSize(400, 400);
    		f_stats.setResizable(false);
    		f_stats.setLocationRelativeTo(this);
    		f_stats.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    		this.setEnabled(false);
    		
    		// Añadimos un listener al frame de estadísticas para que cuando se cierre habilitar el frame principal
    		f_stats.addWindowListener(new WindowAdapter()
    				{
    					@Override
						public void windowClosing(WindowEvent e)
						{
    						f_stats.dispose();
    						cerrando_ventana();
						}
    				});
    		
    		String rut = this.ruta_estadisticas; // Cogemos la ruta del directorio donde se guardan las estadisticas
    		seleccionado = JOptionPane.showOptionDialog(null, "Elige un nivel para ver las estadísticas.","Niveles",JOptionPane.DEFAULT_OPTION,JOptionPane.INFORMATION_MESSAGE,null,opciones_est,opciones_est[0]);
    		if (seleccionado == 0)
    			rut += "/Principiante"; // Si el usuario selecciona principiante, añadimos a la ruta la carpeta necesaria
    		else if (seleccionado == 1)
    			rut += "/Intermedio"; // Si el usuario selecciona intermedio, añadimos a la ruta la carpeta necesaria
    		else if (seleccionado == 2)
    			rut += "/Experto"; // Si el usuario selecciona Experto, añadimos a la ruta la carpeta necesaria
    		else if (seleccionado == 3)
    			rut += "/Personalizado"; // Si el usuario selecciona personalizado, añadimos a la ruta la carpeta necesaria
    		
    		this.e_est = new JTextArea(); // Creamos un area de texto para enseñar las estadísticas
    		String texto = this.get_texto_est(rut); // Cogemos las estadísticas que haya en los archivos de esta ruta
    		e_est.setText(texto); // Inicializamos el texto del area con las estadísticas tomadas
    		e_est.setEditable(false);
    		
    		JScrollPane scroll = new JScrollPane (e_est, 
    				   JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

    		f_stats.add(scroll);
    		f_stats.setVisible(true); // Enseñamos la ventana estadísticas
    	}
    	else if (e.getSource() == m_opc) // Si pulsa el submenu opciones, se abre una ventana donde se puede elegir un nivel determinado
    	{
    		this.detener_crono(); // Paramos el cronómetro mientras estemos aquí
    		this.f_opc = new JFrame("Opciones"); // Creamos el frame opciones
    		f_opc.setSize(340, 220);
    		f_opc.setResizable(false);
    		f_opc.setLocationRelativeTo(this);
    		f_opc.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    		
    		this.b_principiante = new JRadioButton("Principiante"); // Creamos el radiobutton principiante y le añadimos un action listener
    		b_principiante.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{	
					set_personalizado_false();
				}
			});
    		b_intermedio = new JRadioButton("Intermedio"); // Creamos el radiobutton intermedio y le añadimos un action listener
    		b_intermedio.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{	
					set_personalizado_false();
				}
			});
    		b_experto = new JRadioButton("Experto"); // Creamos el radiobutton Experto y le añadimos un action listener
    		b_experto.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{	
					set_personalizado_false();
				}
			});
    		b_pers = new JRadioButton("Personalizado"); // Creamos el radiobutton personalizado y le añadimos un action listener
    		b_pers.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					b_pers.setSelected(true);	
					set_personalizado_true();
				}
			});
    		
    		// Creación de los textos de información para los diferentes niveles
    		e_prin = new JTextArea();
    		e_prin.setOpaque(false);
    		e_prin.setBorder(new TitledBorder(""));
    		e_prin.setFont(b_pers.getFont());
    		e_prin.setText("Tablero 10x10\n10 minas");
    		e_prin.setEditable(false);
    		
    		e_inter = new JTextArea();
    		e_inter.setOpaque(false);
    		e_inter.setBorder(new TitledBorder(""));
    		e_inter.setFont(b_pers.getFont());
    		e_inter.setText("Tablero 16x16\n40 minas");
    		e_inter.setEditable(false);
    		
    		e_exp = new JTextArea();
    		e_exp.setOpaque(false);
    		e_exp.setBorder(new TitledBorder(""));
    		e_exp.setFont(b_pers.getFont());
    		e_exp.setText("Tablero 32x16\n90 minas");
    		e_exp.setEditable(false);
    		
    		panel_pers = new JPanel();
    		panel_pers.setBorder(new TitledBorder(""));
    		l_filas = new JLabel();
    		l_filas.setText("Filas:");
    		l_columnas = new JLabel();
    		l_columnas.setText("Columnas:");
    		l_minas = new JLabel();
    		l_minas.setText("Minas:");
    		t_filas = new JTextField(2);
    		t_columnas = new JTextField(2);
    		t_minas = new JTextField(2);
    		panel_pers.add(l_filas);
    		panel_pers.add(t_filas);
    		panel_pers.add(l_columnas);
    		panel_pers.add(t_columnas);
    		panel_pers.add(l_minas);
    		panel_pers.add(t_minas);
    		this.set_personalizado_false();
    		
    		b_aceptar = new JButton("Aceptar"); // Creamos el botón aceptar y le añadimos un action listener
    		b_aceptar.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					f_opc.dispose();
					cerrando_ventana();
					set_setup();
				}
			});
    		b_cancelar = new JButton("Cancelar"); // Creamos el botón cancelar y le añadimos un action listener
    		b_cancelar.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					f_opc.dispose();
					cerrando_ventana();
				}
			});
    		
    		// Añadimos todo lo anterior a un jpanel que posteriormente añadimos al frame opciones
    		panel_botones = new JPanel();
    		panel_botones.setBorder(new TitledBorder("Dificultad"));
    		grupo = new ButtonGroup();
    	    grupo.add(b_principiante);
    	    grupo.add(b_intermedio);
    	    grupo.add(b_experto);
    	    grupo.add(b_pers);
    	    panel_botones.add(b_principiante);
    	    panel_botones.add(b_intermedio);
    	    panel_botones.add(b_experto);
    	    panel_botones.add(e_prin);
    	    panel_botones.add(e_inter);
    	    panel_botones.add(e_exp);
    	    panel_botones.add(b_pers);
    	    panel_botones.add(panel_pers);
    	    panel_botones.add(b_aceptar);
    	    panel_botones.add(b_cancelar);
    	    
    	    
    	    f_opc.getContentPane().add(panel_botones);
    		f_opc.setVisible(true);
    		this.setEnabled(false);
    		
    		// Añadimos un listener al frame de opciones para que cuando se cierre habilitar el frame principal
    		f_opc.addWindowListener(new WindowAdapter()
    				{
    					@Override
						public void windowClosing(WindowEvent e)
						{
    						f_opc.dispose();
    						cerrando_ventana();
						}
    				});
    	}
    	else if (e.getSource() == m_salir) // Si se pulsa el submenu salir, se sale del juego
    	{
    		System.exit(0);
    	}
    	else if (e.getSource() == this.m_guardar) // Si se pulsa el botón guardar, se llama al método guardar_partida
    	{
    		try 
    		{
				this.guardar_partida();
			} 
    		catch (NotSerializableException e1) 
    		{
				e1.printStackTrace();
			}
    	}
    	else if (e.getSource() == this.m_cargar) // Si se pulsa el botón cargar, se llama al método cargar_partida
    	{
    		try 
    		{
				this.cargar_partida();
			} 
    		catch (ClassNotFoundException e1) 
    		{
				e1.printStackTrace();
			}
    	}
    	/**/
    	
    	else
    	{
    		found =  false;
	        JButton current = (JButton)e.getSource();
	        for (int y = 0;y<m;y++)
	        {
	            for (int x = 0;x<n;x++)
	            {
	                JButton t = b[x][y];
	                if(t == current)
	                {
	                    row=x;column=y; found =true;
	                }
	            }
	        }
	        
	        if(!found) 
	        {
	            System.out.println("didn't find the button, there was an error "); System.exit(-1);
	        }
	        Component temporaryLostComponent = null;
	        
	        if (b[row][column].getBackground() == Color.orange)
	        {
	            return;
	        } 
	        else if (mines[row+1][column+1] == 1)
	        {
	        	
	        	/* Si se encuentra una mina, se colorea de rojo */
	        	this.b[row][column].setBackground(Color.RED);
	        	this.b[row][column].setForeground(Color.BLACK);
	        	this.b[row][column].setText("*");
	        	/**/
	        	
	        	/* Cambiamos el mensaje de juego finalizado y añadimos la opción de reiniciar */
	        	//JOptionPane.showMessageDialog(temporaryLostComponent, "You set off a Mine!!!!."); 
	        	this.detener_crono();
	        	seleccionado = JOptionPane.showOptionDialog(null, "Has encontrado una mina.","Juego finalizado",JOptionPane.DEFAULT_OPTION,JOptionPane.ERROR_MESSAGE,null,opciones,opciones[0]);
	        	if (seleccionado == 0)
	        	{
	        		minas = this.nomines;
	        		filas = this.n;
	        		columnas = this.m;
	        		this.dispose();
	        		new Buscaminas(minas,filas,columnas); // Si el usuario selecciona reiniciar, se inicia un nuevo juego con la configuración anterior
	        	}
	        	else if (seleccionado == 1)
	        		System.exit(0);
	        	/**/
	        	
	        	/* Detenemos el crono */
	        	this.detener_crono();
	        	/**/
	        } 
	        else 
	        {
	            tmp = Integer.toString(perm[row][column]);
	            if (perm[row][column] == 0)
	            {
	            	
	            	/* Ponemos el botón en blanco */
	            	this.b[row][column].setBackground(Color.WHITE);
	            	/**/
	            	
	            	tmp = " ";
	            }
	            
	            /* Ponemos el botón en blanco */
	            this.b[row][column].setBackground(Color.WHITE);
	            /**/
	            
	            b[row][column].setText(tmp);
	            b[row][column].setEnabled(false);
	            checkifend();
	            if (perm[row][column] == 0)
	            {
	                scan(row, column);
	                checkifend();
	            }
	        }
    	}
    }
    
    /**
     * Método para, según qué nivel esté seleccionado, utilizar ese setup en la próxima partida
     */
	protected void set_setup() {
    	if (this.b_principiante.isSelected())
    		this.setup_principiante();
    	else if (this.b_intermedio.isSelected())
    		this.setup_intermedio();
    	else if (this.b_experto.isSelected())
    		this.setup_experto();
    	else if (this.b_pers.isSelected())
    		this.setup_personalizado();
	}

	/* Métodos para poner las configuración que quiera el usuario */
	/**
	 * Método para poner el setup personalizado
	 */
    private void setup_personalizado() {
		this.dispose();
		try 
		{
			Buscaminas bc = new Buscaminas(Integer.parseInt(this.t_minas.getText()),Integer.parseInt(this.t_filas.getText()),Integer.parseInt(this.t_columnas.getText()));
			bc.pers = true; 
		} catch (Exception e)
		{
			
		}
	}
    /**
     * Método para poner not enabled la zona del radiobutton personalizado
     */
    protected void set_personalizado_false()
    {
    	for (Component c : panel_pers.getComponents())
		{
			c.setEnabled(false);
		}
    }
    /**
     * Método para poner enabled la zona del radiobutton personalizado
     */
	protected void set_personalizado_true() {
    	for (Component c : panel_pers.getComponents())
		{
			c.setEnabled(true);
		}
	}
	/**
	 * Método para poner el setup experto
	 */
	protected void setup_experto() {
		this.dispose();
		Buscaminas bc = new Buscaminas(99,32,16);
		bc.exp = true;
	}
	/**
	 * Método para poner el setup intermedio
	 */
	protected void setup_intermedio() {
		this.dispose();
		Buscaminas bc = new Buscaminas(40,16,16);
		bc.inter = true;
	}
	/**
	 * Método para poner el setup principiante
	 */
	protected void setup_principiante() {
		this.dispose();
		Buscaminas bc = new Buscaminas(10,10,10);
		bc.prin = true;
	}
	/**/
	/* Módulo para guardar y cargar partidas en ficheros, además de guardar también las estadísticas */
	/**
	 * Método para guardar una partida. Al no ser serializable el jbutton no hemos podido guardar el objeto entero, así que hemos decidido 
	 * ir guardando objeto a objeto los parámetros que necesitariamos para volver a iniciar un juego con la misma configuración que el 
	 * anterior.
	 * @throws NotSerializableException
	 */
	public void guardar_partida() throws NotSerializableException
	{     
	    String _b[][] = new String[this.n][this.m]; // En vez de una matriz de jbuttons, guardaremos una matriz de strings 
	    for (int i = 0; i < this.n; i++)
	    	for (int j = 0; j < this.m; j++)
	    		_b[i][j] = this.b[i][j].getText(); // Guardamos el valor de texto del jbutton
		
		aux = 1;
		File fichero_padre = new File(this.ruta_guardar); // Creamos un fichero en la ruta elegida para guardar partidas
		fichero_padre.mkdirs(); // Si no existe, lo creamos
		// Recorremos el directorio donde se guardan las partidas para saber qué número de partida se guarda
		for (File arch : fichero_padre.listFiles()) 
			aux++;
		
		ObjectOutputStream archivo;
		try 
		{
			// Creamos el fileoutputstream con el nombre que le toque
			archivo = new ObjectOutputStream(new FileOutputStream(this.ruta_guardar + "/Partida_"+ String.valueOf(aux) + ".dat")); 
			// Vamos guardando ahora en el archivo las variables que necesitamos
			archivo.writeObject(this.perm);
			archivo.writeObject(this.guesses);
			archivo.writeObject(_b);
			archivo.writeObject(this.mines);
			archivo.writeInt(this.n);
			archivo.writeInt(this.m);
			archivo.writeInt(this.nomines);
			archivo.writeInt(this.rem_mines);
			archivo.writeInt(Integer.valueOf(this.t_time.getText()));
			archivo.close(); // Cerramos el archivo
		} 
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	/**
	 * Método para cargar una partida. Este es un método espejo del anterior, simplemente cogemos el archivo seleccionado por el usuario
	 * y volcamos los objetos guardados en él para crear una nueva instancia de la clase Buscaminas con dicha configuración.
	 * @throws ClassNotFoundException
	 */
	public void cargar_partida() throws ClassNotFoundException
	{
		// Variables donde volcaremos los objetos guardados
		int _perm[][] = null;
	    int _guesses[][] = null;
	    String _b[][] = null;
	    int[][] _mines = null;
	    int _n = 0;
	    int _m = 0;
	    int _nomines = 0;
	    int _remmines = 0;
	    int _time = 0;
		
		JFileChooser fc = new JFileChooser(); // Creamos un jfilechooser para que el usuario elija una partida ya guardada
		fc.setCurrentDirectory(new File(ruta_guardar));
		int selecc = fc.showOpenDialog(null);
		File partida = fc.getSelectedFile();
		
		try 
		{
			ObjectInputStream archivo = new ObjectInputStream(new FileInputStream(partida));
			// Leemos paralelamente las variables que hemos guardado 
			_perm = (int[][]) archivo.readObject();
			_guesses = (int[][]) archivo.readObject();
			_b = (String[][]) archivo.readObject();
			_mines = (int[][]) archivo.readObject();
			_n = archivo.readInt();
			_m = archivo.readInt();
			_nomines = archivo.readInt();
			_remmines = archivo.readInt();
			_time = archivo.readInt();
			archivo.close();
		} 
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		// Una vez que tenemos las variables, creamos la matriz de botones y lanzamos una nueva instancia
		this.dispose();
		Buscaminas bc = new Buscaminas(_nomines,_n,_m);
		bc.perm = _perm;
		bc.guesses = _guesses;
		
		// Le añadimos la matriz de botones poniendo otra vez la configuración anterior de cada uno dependiendo de su texto
		for (int i = 0; i < _n; i++)
	    	for (int j = 0; j < _m; j++) {
	    		bc.b[i][j].setText(_b[i][j]);
	    		if (_b[i][j].equals(" "))
	    		{
	    			bc.b[i][j].setBackground(Color.WHITE);
	    			bc.b[i][j].setEnabled(false);
	    		}
	    		else if(_b[i][j].equals("?"))
	    		{
	    			
	    		}
	    		else if(_b[i][j].equals("x"))
	    		{
	    			bc.b[i][j].setBackground(Color.ORANGE);
	    		}
	    		else
	    		{
	    			bc.b[i][j].setBackground(Color.WHITE);
	    			bc.b[i][j].setEnabled(false);
	    		}
	    	}
		
		// Recogemos más objetos
		bc.mines = _mines;
		bc.rem_mines = _remmines;
		bc.t_rem_mines.setText(String.valueOf(_remmines));
		bc.t_time.setText(String.valueOf(_time));
		bc.segundos = _time;
	}
	/**
	 * 
	 * @return
	 */
	public int guardar_estadisticas()
	{
		aux = 1;
		String ruta = this.ruta_estadisticas; // Ruta elegida para guardar las estadísticas
		
		// Dependiendo de la configuración que haya sido elegida, añadimos una carpeta adicional a la ruta
		if (this.prin)
			ruta += "/Principiante"; 
		else if (this.inter)
			ruta += "/Intermedio";
		else if (this.exp) 
			ruta += "/Experto";
		else if (this.pers)
			ruta += "/Personalizado";
		
		File fichero_padre = new File(ruta); // Creamos un fichero con esa ruta
		fichero_padre.mkdirs(); // Si no existe el directorio, lo creamos
		for (File archivo : fichero_padre.listFiles()) // Comprobamos cuantas partidas hay guardadas
			aux++;
		
		if (aux == 11) // Si ya hay 10 partidas guardadas tenemos que comprobar si el tiempo de esta es mejor que el peor tiempo
		{
			if ((int)((endtime-starttime)/1000000000) < devolver_peor(ruta)) // Si es mejor que el peor
			{
				aux = eliminar_peor(ruta); // Eliminamos el archivo con el peor tiempo y recogemos en aux el número de partida que era
			}	 
			else // Si no es mejor que el peor, no guardamos el tiempo
			{
				return 0;
			}
		}
		else // Si hay menos de 10 partidas guardadas
			aux = devolver_ultimo(ruta) + 1; // Cogemos el identificador de partida mayor
				
		FileWriter fichero = null;
        PrintWriter pw = null;
        try
        {
        	Date date = new Date(); // Cogemos la fecha 
        	DateFormat hourdateFormat = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
        	
            fichero = new FileWriter(ruta + "/Partida_" + String.valueOf(aux) + ".txt"); // Le damos nombre al fichero según la variable aux
            pw = new PrintWriter(fichero);
            
            pw.println("Nombre: " + this.nombre_jugador); // Escribimos el nombre del jugador
            pw.println("Fecha: " + hourdateFormat.format(date)); // Escribimos la fecha de la partida jugada
            pw.println();
            pw.println("Tiempo: " + String.valueOf((int)((endtime-starttime)/1000000000))); // Escribimos el tiempo 
            pw.println("Minas: " + String.valueOf(this.nomines)); // Escribimos las minas
            pw.println("Filas: " + String.valueOf(this.n)); // Escribimos las filas
            pw.println("Columnas: " + String.valueOf(this.m)); // Escribimos las columnas
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
           try {
           if (null != fichero)
              fichero.close();
           } catch (Exception e2) {
              e2.printStackTrace();
           }
        }
        return 1;
	}
	/**
	 * Método que devuelve de un directorio, pasado como parámetro, con ficheros, el número más alto que haya en sus nombres
	 * @param ruta
	 * @return
	 */
	private int devolver_ultimo(String ruta) 
	{
		int mayor = 0, actual; // Numero mayor para comparar
		FileReader fr = null;
		BufferedReader br;
		String nombre;
		char[] nombrech;
		File fichero_padre = new File(ruta); // Ruta donde se guardan las estadísticas de cada partida
		fichero_padre.mkdirs();
		for (File archivo : fichero_padre.listFiles()) // Recorremos los archivos con las estadisticas
		{
			try
			{
				fr = new FileReader(archivo);
				br = new BufferedReader(fr);
				
				nombre = archivo.getName();
				nombrech = nombre.toCharArray();
				nombre = ""; // Utilizamos nombre para introducir el numero de partida
				if (nombrech.length == 13) // Tiene una cifra en el numero de partida
				{
					nombre += nombrech[8];
					actual = Integer.valueOf(nombre);
					if (actual > mayor) // Si el numero es mayor que el mayor ya guardado
						mayor = actual; // Lo sobreescribimos
				}
				else if (nombrech.length == 14) // Tiene dos cifras en el numero de partida
				{
					nombre += nombrech[8];
					nombre += nombrech[9];
					actual = Integer.valueOf(nombre);
					if (actual > mayor) // Si el número es mayor que el mayor ya guardado
						mayor = actual; // Lo sobreescribimos
				}
			}
		    catch(Exception e)
			{
		    	e.printStackTrace();
			}
			finally
			{
				try
				{                    
					if( null != fr )
					{   
						fr.close();     
		            }                  
		        }
				catch (Exception e2)
				{ 
		            e2.printStackTrace();
		        }
		    }
		}
		return mayor; // Devolvemos el mayor
	}
	/**
	 * Método que busca dentro de un directorio el archivo con peor tiempo y lo elimina, además de devolver el número de partida que es
	 * @param ruta
	 * @return
	 */
	private int eliminar_peor(String ruta) 
	{
		int actual, peor = 0; // Peor tiempo para comparar
		int a;
		FileReader fr = null;
		BufferedReader br;
		String linea, time, nombre;
		char[] lineach, nombrech;
		File fichero_padre = new File(ruta); // Ruta donde se guardan las estadísticas de cada partida
		File fichero_a_borrar = null; // Fichero con peor tiempo que borraremos
		fichero_padre.mkdirs();
		for (File archivo : fichero_padre.listFiles()) // Recorremos los archivos con las estadisticas
		{
			time = ""; // Inicializamos el string time
			a = 0;
			try
			{
				fr = new FileReader(archivo);
				br = new BufferedReader(fr);
				
				// Nos ponemos en la linea del tiempo
				do
				{
					br.readLine();
					a++;
				} while (a != 3);
				
				linea = br.readLine();
				lineach = linea.toCharArray(); // Pasamos el string a char array para leer el tiempo
				if (lineach.length == 9) // Comprobamos si el tiempo tiene una cifra
				{
					time += lineach[8];
					actual = Integer.valueOf(time);
					if (actual > peor) // Si el tiempo actual es mayor que el peor
					{
						peor = actual; // Actualizamos el peor tiempo con el actual
						fichero_a_borrar = new File(archivo.getAbsolutePath());
					}	
				}
				else if (lineach.length == 10) // Dos cifras
				{
					time += lineach[8];
					time += lineach[9];
					actual = Integer.valueOf(time);
					if (actual > peor) // Si el tiempo actual es mayor que el peor
					{
						peor = actual; // Actualizamos el peor tiempo con el actual
						fichero_a_borrar = new File(archivo.getAbsolutePath());
					}
						
				}
				else if (lineach.length == 11) // Tres cifras
				{
					time += lineach[8];
					time += lineach[9];
					time += lineach[10];
					actual = Integer.valueOf(time);
					if (actual > peor) // Si el tiempo actual es mayor que el peor
					{
						peor = actual; // Actualizamos el peor tiempo con el actual
						fichero_a_borrar = new File(archivo.getAbsolutePath());
					}
				}
			}
		    catch(Exception e)
			{
		    	e.printStackTrace();
			}
			finally
			{
				try
				{                    
					if( null != fr )
					{   
						fr.close();     
		            }                  
		        }
				catch (Exception e2)
				{ 
		            e2.printStackTrace();
		        }
		    }
		}
		nombre = fichero_a_borrar.getName();
		nombrech = nombre.toCharArray();
		nombre = ""; // Utilizamos nombre para introducir el numero de partida
		fichero_a_borrar.delete(); // Borramos ese fichero
		if (nombrech.length == 13) // Tiene una cifra en el numero de partida
		{
			nombre += nombrech[8];
			return Integer.valueOf(nombre);
		}
		else if (nombrech.length == 14) // Tiene una cifra en el numero de partida
		{
			nombre += nombrech[8];
			nombre += nombrech[9];
			return Integer.valueOf(nombre);
		}
		else
			return 0;
	}
	/**
	 * Método que devuelve el peor tiempo de una serie de archivos que se encuentren en un directorio pasado como parametro
	 * @param ruta
	 * @return
	 */
	private int devolver_peor(String ruta) 
	{
		int actual, peor = 0; // Peor tiempo para comparar
		int a;
		FileReader fr = null;
		BufferedReader br;
		String linea, time;
		char[] lineach;
		File fichero_padre = new File(ruta); // Ruta donde se guardan las estadísticas de cada partida
		fichero_padre.mkdirs();
		for (File archivo : fichero_padre.listFiles()) // Recorremos los archivos con las estadisticas
		{
			a = 0;
			time = ""; // Inicializamos el string time
			try
			{
				fr = new FileReader(archivo);
				br = new BufferedReader(fr);
				
				// Nos ponemos en la linea del tiempo
				do
				{
					br.readLine();
					a++;
				} while (a != 3);
				
				linea = br.readLine();
				lineach = linea.toCharArray(); // Pasamos el string a char array para leer el tiempo
				if (lineach.length == 9) // Comprobamos si el tiempo tiene una cifra
				{
					time += lineach[8];
					actual = Integer.valueOf(time);
					if (actual > peor) // Si el tiempo actual es mayor que el peor
						peor = actual; // Actualizamos el peor tiempo con el actual
				}
				else if (lineach.length == 10) // Dos cifras
				{
					time += lineach[8];
					time += lineach[9];
					actual = Integer.valueOf(time);
					if (actual > peor) // Si el tiempo actual es mayor que el peor
						peor = actual; // Actualizamos el peor tiempo con el actual
				}
				else if (lineach.length == 11) // Tres cifras
				{
					time += lineach[8];
					time += lineach[9];
					time += lineach[10];
					actual = Integer.valueOf(time);
					if (actual > peor) // Si el tiempo actual es mayor que el peor
						peor = actual; // Actualizamos el peor tiempo con el actual
				}
			}
		    catch(Exception e)
			{
		    	e.printStackTrace();
			}
			finally
			{
				try
				{                    
					if( null != fr )
					{   
						fr.close();     
		            }                  
		        }
				catch (Exception e2)
				{ 
		            e2.printStackTrace();
		        }
		    }
		}
		return peor;	
	}
	/**
	 * Método que devuelve el texto perteneciente a cada uno de los archivos de un directorio pasado como parámetro
	 * @param r
	 * @return
	 */
	private String get_texto_est(String r) 
	{
		String linea;
		String texto = ""; // Inicializamos el texto
		String ruta = r;
		FileReader fr = null;
		BufferedReader br;
		
		File fichero_padre = new File(ruta); // Ruta donde se guardan las estadísticas de cada partida
		fichero_padre.mkdirs();
		for (File archivo : fichero_padre.listFiles()) // Recorremos los archivos con las estadisticas
		{
			try
			{
				fr = new FileReader(archivo);
				br = new BufferedReader(fr);
				
				while ((linea = br.readLine()) != null)
				{
					texto += linea + "\n"; // Concatenamos el texto del archivo al texto de salida
				}
				texto += "\n--------------------------------------------\n";
			}
		    catch(Exception e)
			{
		    	e.printStackTrace();
			}
			finally
			{
				try
				{                    
					if( null != fr )
					{   
						fr.close();     
		            }                  
		        }
				catch (Exception e2)
				{ 
		            e2.printStackTrace();
		        }
		    }
		}
		return texto;
	}
	/**/
	
	public void checkifend()
    {
        int check= 0;
        for (int y = 0; y<m;y++)
        {
            for (int x = 0;x<n;x++)
            {
		        if (b[x][y].isEnabled()){
		            check++;
		        }
            }
        }
        if (check == nomines)
        {
            endtime = System.nanoTime();
            Component temporaryLostComponent = null;
            
            /* Cambiamos el mensaje de dialogo al ganar */
            //JOptionPane.showMessageDialog(temporaryLostComponent, "Congratulations you won!!! It took you "+(int)((endtime-starttime)/1000000000)+" seconds!");
            this.detener_crono();
            this.nombre_jugador = JOptionPane.showInputDialog(null,"!Victoria! Ha necesitado: " + (int)((endtime-starttime)/1000000000) + " segundos\n" + "Escriba su nombre");
            this.seleccionado2 = JOptionPane.showOptionDialog(null, "¿Desea guardar las estadísticas de la partida?","Guardar Estadísticas",JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,null,opciones2,opciones2[0]);
            
            if (seleccionado2 == 0)
            {
            	this.guardar_estadisticas();
            }
            
            this.dispose();
            new Buscaminas(40,16,16); // Empezamos una partida por defecto
            /**/
        }
    }
 
    public void scan(int x, int y)
    {
        for (int a = 0;a<8;a++)
        {
            if (mines[x+1+deltax[a]][y+1+deltay[a]] == 3)
            {
            	// Aqui falta codigo
            } else if ((perm[x+deltax[a]][y+deltay[a]] == 0) && (mines[x+1+deltax[a]][y+1+deltay[a]] == 0) && (guesses[x+deltax[a]+1][y+deltay[a]+1] == 0))
            {
                if (b[x+deltax[a]][y+deltay[a]].isEnabled())
                {
                	/* Ponemos el botón en blanco */
                	this.b[x+deltax[a]][y+deltay[a]].setBackground(Color.WHITE);
                	/**/
                    b[x+deltax[a]][y+deltay[a]].setText(" ");
                    b[x+deltax[a]][y+deltay[a]].setEnabled(false);
                    scan(x+deltax[a], y+deltay[a]);
                }
            } else if ((perm[x+deltax[a]][y+deltay[a]] != 0) && (mines[x+1+deltax[a]][y+1+deltay[a]] == 0)  && (guesses[x+deltax[a]+1][y+deltay[a]+1] == 0))
            {
                tmp = new Integer(perm[x+deltax[a]][y+deltay[a]]).toString();
                /* Ponemos el botón en blanco */
                this.b[x+deltax[a]][y+deltay[a]].setBackground(Color.WHITE);
                /**/
                b[x+deltax[a]][y+deltay[a]].setText(Integer.toString(perm[x+deltax[a]][y+deltay[a]]));
                b[x+deltax[a]][y+deltay[a]].setEnabled(false);
            }
        }
    }
 
    public int perimcheck(int a, int y)
    {
        int minecount = 0;
        for (int x = 0;x<8;x++)
        {
            if (mines[a+deltax[x]+1][y+deltay[x]+1] == 1)
            {
                minecount++;
            }
        }
        return minecount;
    }
 
    public void windowIconified(WindowEvent e){
 
    }
 
    public static void main(String[] args){
    	new Buscaminas(40,16,16); // Por defecto empieza en intermedio
    }
 
    public void mouseClicked(MouseEvent e) {
 
    }
 
    @Override
    public void mouseEntered(MouseEvent arg0) {
        // TODO Auto-generated method stub
    }
 
    @Override
    public void mouseExited(MouseEvent arg0) {
        // TODO Auto-generated method stub
    }
 
    @Override
    public void mousePressed(MouseEvent e) 
    {
        if (e.getButton() == MouseEvent.BUTTON3) 
        {
            found =  false;
            Object current = e.getSource();
            for (int y = 0;y<m;y++)
            {
            	for (int x = 0;x<n;x++)
            	{
            		JButton t = b[x][y];
                    	if(t == current)
                    	{
                    		row=x;column=y; found =true;
                        }
                }
            }
            if(!found) 
            {
                System.out.println("didn't find the button, there was an error "); System.exit(-1);
            }
            if ((guesses[row+1][column+1] == 0) && (b[row][column].isEnabled()))
            {
                b[row][column].setText("x");
                guesses[row+1][column+1] = 1;
                b[row][column].setBackground(Color.orange);
                /* Si el usuario marca una mina, restamos una a las restantes */
                rem_mines--; 
                this.t_rem_mines.setText(String.valueOf(rem_mines));
                /**/
            } else if (guesses[row+1][column+1] == 1)
            {
                b[row][column].setText("?");
                guesses[row+1][column+1] = 0;
                b[row][column].setBackground(null);
                /* Si el usuario desmarca una mina, sumamos una a las restantes */
                rem_mines++; 
                this.t_rem_mines.setText(String.valueOf(rem_mines));
                /**/
            }
        }
    }
 
    @Override
    public void mouseReleased(MouseEvent arg0) {
        // TODO Auto-generated method stub
    }
}
