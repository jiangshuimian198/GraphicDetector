package main.java.ui;

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import main.java.Extractor.KnowledgeExtractor;



public class MainWindow {

	private JFrame frmv;
	private JTextField projectDirectory;
	private JTextField graphDirectory;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainWindow window = new MainWindow();
					window.frmv.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public MainWindow() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmv = new JFrame();
		frmv.setResizable(false);
		frmv.setTitle("缺陷检测-v1.0");
		frmv.setBounds(100, 100, 738, 367);
		frmv.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmv.getContentPane().setLayout(null);
		
		JLabel project = new JLabel("请选择项目目录：");
		project.setFont(new Font("宋体", Font.PLAIN, 22));
		project.setBounds(30, 98, 198, 21);
		frmv.getContentPane().add(project);
		
		projectDirectory = new JTextField();
		projectDirectory.setFont(new Font("宋体", Font.PLAIN, 22));
		projectDirectory.setEditable(false);
		projectDirectory.setBounds(219, 94, 334, 27);
		frmv.getContentPane().add(projectDirectory);
		projectDirectory.setColumns(10);
		projectDirectory.setText("D:\\\\intellide-graph-master");
		
		JButton browseProject = new JButton("浏览");
		browseProject.setFont(new Font("宋体", Font.PLAIN, 22));
		browseProject.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int returnVal = chooser.showOpenDialog(new JPanel());
				File f = chooser.getSelectedFile();
				if(returnVal == JFileChooser.APPROVE_OPTION) {
				projectDirectory.setText(f.getAbsolutePath());}
			}
		});
		browseProject.setBounds(583, 94, 123, 29);
		frmv.getContentPane().add(browseProject);
		
		JLabel graph = new JLabel("请选择导出目录：");
		graph.setFont(new Font("宋体", Font.PLAIN, 22));
		graph.setBounds(30, 162, 198, 21);
		frmv.getContentPane().add(graph);
		
		graphDirectory = new JTextField();
		graphDirectory.setFont(new Font("宋体", Font.PLAIN, 22));
		graphDirectory.setEditable(false);
		graphDirectory.setColumns(10);
		graphDirectory.setBounds(219, 158, 334, 27);
		frmv.getContentPane().add(graphDirectory);
		graphDirectory.setText("D:\\neo4j-community-3.5.1\\data\\databases\\graph.db");
		
		JButton browseGraph = new JButton("浏览");
		browseGraph.setFont(new Font("宋体", Font.PLAIN, 22));
		browseGraph.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int returnVal = chooser.showOpenDialog(new JPanel());
				File f = chooser.getSelectedFile();
				if(returnVal == JFileChooser.APPROVE_OPTION) {
				graphDirectory.setText(f.getAbsolutePath());}
			}
		});
		browseGraph.setBounds(583, 158, 123, 29);
		frmv.getContentPane().add(browseGraph);
		
		JButton Extraction = new JButton("抽取");
		Extraction.setFont(new Font("宋体", Font.PLAIN, 22));
		Extraction.setBounds(219, 238, 154, 29);
		frmv.getContentPane().add(Extraction);
		
		JButton Cancel = new JButton("取消");
		Cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				projectDirectory.setText("");
				graphDirectory.setText("");
			}
		});
		Cancel.setFont(new Font("宋体", Font.PLAIN, 22));
		Cancel.setBounds(399, 238, 154, 29);
		frmv.getContentPane().add(Cancel);
		Extraction.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frmv.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
				frmv.setTitle("正在提取，请稍候......");
				Cancel.setEnabled(false);
				Extraction.setEnabled(false);
				
				File yamlFile = new File("config.yaml");
				String graphDir = "graphDir: "+graphDirectory.getText();
				String projectDir = "main.java.JCExtractor.JavaExtractor: "+projectDirectory.getText()+"\\src";
				try {
					@SuppressWarnings("resource")
					BufferedWriter bfw = new BufferedWriter(new FileWriter(yamlFile));
					bfw.write(graphDir+"\r\n");
					bfw.flush();
					bfw.write(projectDir+"\r\n");
					bfw.flush();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				try {
					Process process = Runtime.getRuntime().exec("cmd.exe /c neo4j stop");
					process.waitFor();
				} catch (IOException | InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				boolean extractionCompleted = KnowledgeExtractor.extract(yamlFile);
				if(extractionCompleted) {
					JOptionPane.showMessageDialog(Extraction, "抽取完成！");
					frmv.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					frmv.setTitle("缺陷检测-v1.0");
					Cancel.setEnabled(true);
					Extraction.setEnabled(true);
					try {
						Process process = Runtime.getRuntime().exec("cmd.exe /c neo4j start");
						process.waitFor();
					} catch (IOException | InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		});
		
		
		JLabel extractionPhase = new JLabel("代码抽取");
		extractionPhase.setFont(new Font("黑体", Font.PLAIN, 22));
		extractionPhase.setBounds(30, 36, 154, 21);
		frmv.getContentPane().add(extractionPhase);
		
	}
}
