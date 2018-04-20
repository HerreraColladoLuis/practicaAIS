import java.awt.*;
import java.awt.event.*;
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

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

/* ACLARACIONES SOBRE EL CÓDIGO AÑADIDO
Las implementaciones añadidas irán comentadas mediante un comentario multilinea, 
poniendo una linea antes la explicación de dicho código y una linea sin comentar 
al finalizar */

public class Buscaminas extends JFrame implements Runnable, ActionListener, MouseListener, Serializable
{
    int nomines;
    
    /* Añadimos las variables que vamos a necesitar para las mejoras */
    private int rem_mines, milesimas = 0, segundos = 0, seleccionado, seleccionado2, aux, minas, filas, columnas; 
    private JMenuBar menu; // Variable para el menú
    private JMenu m_juego, m_ayuda; // Menú para la pestaña 'juego' y 'ayuda'
    private JMenuItem m_nuevo, m_stats, m_opc, m_salir, m_guardar, m_cargar; // Items del menú juego
    private JFrame f_stats, f_opc; // Ventanas para las estadísticas y para las opciones
    private JRadioButton b_principiante, b_intermedio, b_experto, b_pers; // Botones para elegir la dificultad del juego
    private ButtonGroup grupo; // Grupo de botones para los radiobutton
    private JPanel panel_botones, panel_pers; // Panel que contendrá los botones radiobutton y panel para personalizado
    private JTextArea e_prin, e_inter, e_exp; // Etiquetas para los distintos tipos de juego
    private JLabel l_filas, l_columnas, l_minas, l_rem_mines, t_rem_mines, l_time, t_time;
    private JTextField t_filas, t_columnas, t_minas;
    private JButton b_aceptar, b_cancelar;
    private final String ruta_guardar = "Temp/Partidas", ruta_estadisticas = "Temp/Estadisticas";
    private String nombre_jugador;
    private Thread proceso_cronometro; // Hilo para correr el contador de segundos
    private Boolean crono_activo;
    private String[] opciones ={"Empezar de nuevo", "Salir"};
    private String[] opciones2 = {"Sí","No"};
    /**/ 
    
    int perm[][];
    String tmp;
    boolean found = false;
    int row;
    int column;
    int guesses[][];
    JButton b[][];
    int[][] mines;
    boolean allmines;
    int n;
    int m;
    int deltax[] = {-1, 0, 1, -1, 1, -1, 0, 1};
    int deltay[] = {-1, -1, -1, 0, 0, 1, 1, 1};
    double starttime;
    double endtime;
    
    public Buscaminas(int mi, int fi, int col)
    {
    	
    	/* Inicializamos las minas, las filas, las columnas y las minas restantes*/
    	this.nomines = mi;
    	this.n = fi;
    	this.m = col;
    	this.rem_mines = nomines;
    	/**/
    	
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        
        /* Inicialización del menú */
        menu = new JMenuBar();
        this.setJMenuBar(menu);
        
        m_juego = new JMenu("Juego");
        m_juego.setMnemonic(KeyEvent.VK_J);
        menu.add(m_juego);
        
        m_nuevo = new JMenuItem("Nuevo juego");
        m_nuevo.setMnemonic(KeyEvent.VK_N);
		m_nuevo.addActionListener(this);
        m_juego.add(m_nuevo);
        m_juego.addSeparator();
        
        m_stats = new JMenuItem("Estadisticas");
        m_stats.setMnemonic(KeyEvent.VK_E);
        m_stats.addActionListener(this);
        m_juego.add(m_stats);
        
        m_opc = new JMenuItem("Opciones");
        m_opc.setMnemonic(KeyEvent.VK_O);
        m_opc.addActionListener(this);
        m_juego.add(m_opc);
        m_juego.addSeparator();
        
        m_guardar = new JMenuItem("Guardar");
        m_guardar.setMnemonic(KeyEvent.VK_G);
        m_guardar.addActionListener(this);
        m_juego.add(m_guardar);
        
        m_cargar = new JMenuItem("Cargar");
        m_cargar.setMnemonic(KeyEvent.VK_C);
        m_cargar.addActionListener(this);
        m_juego.add(m_cargar);
        m_juego.addSeparator();
        
        m_salir = new JMenuItem("Salir");
        m_salir.setMnemonic(KeyEvent.VK_S);
        m_salir.addActionListener(this);
        m_juego.add(m_salir);
        
        m_ayuda = new JMenu("Ayuda");
        m_ayuda.setMnemonic(KeyEvent.VK_A);
        menu.add(m_ayuda);
        /**/
        
        perm = new int[n][m];
        boolean allmines = false;
        guesses = new int [n+2][m+2];
        mines = new int[n+2][m+2];
        b = new JButton [n][m];
        setLayout(new GridLayout(n+1,m));
        
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
        do 
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
                b[x][y] = new JButton("?");
                
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
    	this.proceso_cronometro.stop();
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
    
    public void actionPerformed(ActionEvent e)
    {
    	/* Comprobamos qué acción está llevando a cabo el usuario */
    	if (e.getSource() == m_nuevo)
    	{
    		minas = this.nomines;
    		filas = this.n;
    		columnas = this.m;
    		this.dispose();
    		new Buscaminas(minas,filas,columnas);
    	}
    	else if (e.getSource() == m_stats)
    	{
    		this.detener_crono();
    		this.f_stats = new JFrame("Estadísticas");
    		f_stats.setSize(400, 400);
    		f_stats.setResizable(false);
    		f_stats.setLocationRelativeTo(this);
    		f_stats.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    		f_stats.setVisible(true);
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
    	}
    	else if (e.getSource() == m_opc)
    	{
    		this.detener_crono();
    		this.f_opc = new JFrame("Opciones");
    		f_opc.setSize(340, 220);
    		f_opc.setResizable(false);
    		f_opc.setLocationRelativeTo(this);
    		f_opc.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    		
    		this.b_principiante = new JRadioButton("Principiante");
    		b_principiante.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{	
					set_personalizado_false();
				}
			});
    		b_intermedio = new JRadioButton("Intermedio");
    		b_intermedio.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{	
					set_personalizado_false();
				}
			});
    		b_experto = new JRadioButton("Experto");
    		b_experto.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{	
					set_personalizado_false();
				}
			});
    		b_pers = new JRadioButton("Personalizado");
    		b_pers.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					b_pers.setSelected(true);	
					set_personalizado_true();
				}
			});
    		
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
    		
    		b_aceptar = new JButton("Aceptar");
    		b_aceptar.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					f_opc.dispose();
					cerrando_ventana();
					set_setup();
				}
			});
    		b_cancelar = new JButton("Cancelar");
    		b_cancelar.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					f_opc.dispose();
					cerrando_ventana();
				}
			});
    		
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
    	else if (e.getSource() == m_salir)
    	{
    		System.exit(0);
    	}
    	else if (e.getSource() == this.m_guardar)
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
    	else if (e.getSource() == this.m_cargar)
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
	        		new Buscaminas(minas,filas,columnas);
	        	}
	        	else if (seleccionado == 1)
	        		System.exit(0);
	        	/**/
	        	
	        	/* Detenemos el crono */
	        	this.detener_crono();
	        	/**/
	        	/* Quitamos la salida del programa */
	        	//System.exit(0);
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
    private void setup_personalizado() {
		this.dispose();
		try 
		{
			new Buscaminas(Integer.parseInt(this.t_minas.getText()),Integer.parseInt(this.t_filas.getText()),Integer.parseInt(this.t_columnas.getText()));
		} catch (Exception e)
		{
			
		}
	}

    protected void set_personalizado_false()
    {
    	for (Component c : panel_pers.getComponents())
		{
			c.setEnabled(false);
		}
    }
    
	protected void set_personalizado_true() {
    	for (Component c : panel_pers.getComponents())
		{
			c.setEnabled(true);
		}
	}

	protected void setup_experto() {
		this.dispose();
		new Buscaminas(99,32,16);
	}

	protected void setup_intermedio() {
		this.dispose();
		new Buscaminas(40,16,16);
	}

	protected void setup_principiante() {
		this.dispose();
		new Buscaminas(10,10,10);
	}
	/**/
	
	/* Módulo para guardar y cargar partidas en ficheros */
	public void guardar_partida() throws NotSerializableException
	{
		aux = 1;
		File fichero_padre = new File(this.ruta_guardar);
		for (File archivo : fichero_padre.listFiles()) // Comprobamos cuantas partidas hay guardadas
			aux++;

		ObjectOutputStream archivo;
		try 
		{
			this.detener_crono();
			archivo = new ObjectOutputStream(new FileOutputStream(this.ruta_guardar + "/Partida " + String.valueOf(aux) + ".dat"));
			archivo.writeObject(this);
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
	}
	
	public void cargar_partida() throws ClassNotFoundException
	{
		
		JFileChooser fc = new JFileChooser();
		fc.setCurrentDirectory(new File(ruta_guardar));
		int selecc = fc.showOpenDialog(null);
		File partida = fc.getSelectedFile();
		
		try 
		{
			ObjectInputStream archivo = new ObjectInputStream(new FileInputStream(partida));
			Buscaminas bc = (Buscaminas) archivo.readObject();
			archivo.close();
			this.dispose();
			bc.setVisible(true);
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
	
	public void guardar_estadisticas()
	{
		aux = 1;
		File fichero_padre = new File(this.ruta_estadisticas);
		for (File archivo : fichero_padre.listFiles()) // Comprobamos cuantas partidas hay guardadas
			aux++;
		
		FileWriter fichero = null;
        PrintWriter pw = null;
        try
        {
        	Date date = new Date();
        	DateFormat hourdateFormat = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
        	
            fichero = new FileWriter(this.ruta_estadisticas + "/Partida " + String.valueOf(aux) + ".txt");
            pw = new PrintWriter(fichero);
            
            pw.println("Nombre: " + this.nombre_jugador);
            pw.println("Fecha: " + hourdateFormat.format(date));
            pw.println();
            pw.println("Tiempo: " + String.valueOf((int)((endtime-starttime)/1000000000)));
            pw.println("Minas: " + String.valueOf(this.nomines));
            pw.println("Filas: " + String.valueOf(this.n));
            pw.println("Columnas: " + String.valueOf(this.m));
            
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
	}
	/**/
	
	/* Módulo para calcular las estadísticas por persona buscada*/
	public int[] estadisticas(String nombre)
	{
		int[] salida = new int[4]; // Devolvemos las minas, filas, columnas y tiempo
		FileReader fr = null;
		BufferedReader br;
		File fichero_padre = new File(this.ruta_estadisticas); // Ruta donde se guardan las estadísticas de cada partida
		for (File archivo : fichero_padre.listFiles()) // Recorremos los archivos buscando las partidas que coincidan con el nombre
		{
			try 
			{
		         fr = new FileReader (archivo);
		         br = new BufferedReader(fr);

		         boolean iguales = true;
		         char[] lineach;
		         char[] nombrech = nombre.toCharArray();
		         int c = 8;
		         int c2 = 0;
		         String linea = br.readLine(); // Leemos solo la primera linea para saber si coincide el nombre
		         lineach = linea.toCharArray();
		         if (linea != null)
		         {
		        	 // En el caracter 8 empieza el nombre
		        	 while (iguales && nombre.length() > c2+1)
		        	 {
		        		 if (lineach[c] != nombrech[c2])
		        			 iguales = false;
		        		 c++;
		        		 c2++;
		        	 }
		        	 
		        	 if (iguales) // Si el nombre coincide, cogemos las estadisticas
		        	 {
		        		 // TO DO
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
		return salida;
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
