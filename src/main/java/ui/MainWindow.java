package main.java.ui;

import main.java.Extractor.KnowledgeExtractor;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JFileChooser;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import java.awt.Font;
import javax.swing.JProgressBar;



public class MainWindow {

	private JFrame frmv;
	private JTextField projectDirectory;
	private JTextField graphDirectory;
	private final Action action = new SwingAction();

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
		Extraction.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File yamlFile = new File("process.yaml");
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
				ExtractionProgress progressBar = new ExtractionProgress();
				progressBar.setBounds(0, 306, 732, 21);
				progressBar.setStringPainted(true);
				frmv.getContentPane().add(progressBar);
				
				Thread t = new Thread(progressBar);
				t.start();
				KnowledgeExtractor.extract(yamlFile);
			}
		});
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
		
		JLabel extractionPhase = new JLabel("代码抽取");
		extractionPhase.setFont(new Font("黑体", Font.PLAIN, 22));
		extractionPhase.setBounds(30, 36, 154, 21);
		frmv.getContentPane().add(extractionPhase);
		
	}
	private class SwingAction extends AbstractAction {
		public SwingAction() {
			putValue(NAME, "SwingAction");
			putValue(SHORT_DESCRIPTION, "Some short description");
		}
		public void actionPerformed(ActionEvent e) {
		}
	}
	private class ExtractionProgress extends JProgressBar implements Runnable{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			for (int i = 0; i <= 100; i++) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				this.setValue(i);
			}
			JOptionPane.showMessageDialog(this, "抽取完成！");
		}
		
	}
}
