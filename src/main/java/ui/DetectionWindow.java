package main.java.ui;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.JSplitPane;
import java.awt.GridLayout;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JList;
import javax.swing.JTextArea;
import java.awt.EventQueue;
import java.awt.Font;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;

public class DetectionWindow extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private static JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					DetectionWindow window = new DetectionWindow();
					window.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 * @throws IOException 
	 */
	public DetectionWindow() throws IOException {
		setTitle("GraphicDetector-v1.0");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1302, 742);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new GridLayout(0, 1, 0, 0));
		
		JSplitPane splitPane = new JSplitPane();
		contentPane.add(splitPane);
		
		JList<Object> list = new JList<Object>();
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		DefaultListModel<Object> defaultListModel = new DefaultListModel<Object>();
		String[] strs = new String[10];
		int i = 0;
		for(String ele : strs)
		{
			ele = i+".txt";
			i++;
			defaultListModel.addElement(ele);
		}
		
		list.setModel(defaultListModel);
		
		JScrollPane leftScrollPane = new JScrollPane();
		leftScrollPane.setViewportView(list);
		
		splitPane.setLeftComponent(leftScrollPane);
		splitPane.setRightComponent(null);
		
		list.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				// TODO Auto-generated method stub
				if(splitPane.getRightComponent() == null) {
				splitPane.setRightComponent(tabbedPane);
				splitPane.setDividerLocation(0.5);
				}
				
				JScrollPane scrollPane = new JScrollPane();
				tabbedPane.addTab((String) list.getSelectedValue(), null, scrollPane, null);
				
				JTextArea textArea = new JTextArea();
				textArea.setFont(new Font("Consolas", Font.PLAIN, 20));
				scrollPane.setViewportView(textArea);
				
				LineNumberHeaderView lineNumberHeaderView = new LineNumberHeaderView();
				lineNumberHeaderView.setLineHeight(24);
//				scrollPane.setRowHeaderView(lineNumberHeaderView);
				
				File f = new File((String) list.getSelectedValue());
				FileReader fr;
				BufferedReader br;
				try {
					fr = new FileReader(f);
					br = new BufferedReader(fr);
					String content = br.readLine();
					while(content != null) {
						textArea.setText(textArea.getText()+content+"\r\n");
						content = br.readLine();
					}
					br.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			
		});
		
	}

}
