
package beatbox;

/**
 *
 * @author AB
 */
import javax.swing.*;
import java.awt.*;
import javax.sound.midi.*;
import java.util.*;
import java.awt.event.*;
import java.io.*;
public class BeatBox {

    JPanel BBPanel;
    ArrayList<JCheckBox> checkBoxes;
    Sequencer sequencer;
    Sequence sequence;
    Track gaana;
    JFrame frame;
    MyDrawPanel drawPanel;
    
    String[] instrumentName ={"Bass Drum","Closed Hi-Hat", "Open Hi-Hat",
        "Acoustic Snare"
    ,"Crash Cymbal","Hand Clap", "High Tom", "Hi Bongo", "Maracas", "Whistle", 
    "Low Conga"
    ,"Cowbell", "Vibraslap", "Low-mid Tom", "High Agogo", "Open Hi-Conga"};
    
    int[] instruments ={35,42,46,38,49,39,50,60,70,72,64,56,58,47,67,63};
    
    public static void main(String[] args) {
        new BeatBox().buildGUI();
    }
    
    public void buildGUI(){
        frame = new JFrame("Cyber BeatBox");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        BorderLayout layout = new BorderLayout();
        JPanel background = new JPanel(layout);
        background.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        
        checkBoxes = new ArrayList<JCheckBox>();
        
        Box buttonBox = new Box(BoxLayout.Y_AXIS);
        
        JButton start = new JButton("Start");
        //start.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        start.addActionListener(new startListener());
        buttonBox.add(start);
        //buttonBox.add(Box.createRigidArea(new Dimension(15,0)));
        
        JButton stop = new JButton("Stop");
        //stop.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        stop.addActionListener(new stopListener());
        buttonBox.add(stop);
       
        JButton upTempo = new JButton("Tempo Up");
        //upTempo.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        upTempo.addActionListener(new upTempoListener());
        buttonBox.add(upTempo);
        
        JButton downTempo = new JButton("Tempo Down");
        //downTempo.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        downTempo.addActionListener(new downTempoListener());
        buttonBox.add(downTempo);
        
        JButton saveIt = new JButton("Save");
        saveIt.addActionListener(new saveGaanaListener());
        buttonBox.add(saveIt);
        
        JButton loadIt = new JButton("Load");
        loadIt.addActionListener(new loadGaanaListener());
        buttonBox.add(loadIt);
        
        JButton clearIt = new JButton("Clear");
        clearIt.addActionListener(new clearCheckBoxListener());
        buttonBox.add(clearIt);
        
        Box nameBox = new Box(BoxLayout.Y_AXIS);
        for(int i=0 ; i<16; i++)
            nameBox.add(new Label(instrumentName[i]));
        
        drawPanel = new MyDrawPanel();
        drawPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        buttonBox.add(drawPanel);
        background.add(BorderLayout.EAST, buttonBox);
        background.add(BorderLayout.WEST, nameBox);
        /**/
        frame.getContentPane().add(background);
        
        GridLayout grid = new GridLayout(16,16);
        grid.setVgap(1);
        grid.setHgap(2);
 
        BBPanel = new JPanel(grid);
        background.add(BorderLayout.CENTER, BBPanel);
        
        for(int i =0; i<256; i++){
            JCheckBox c = new JCheckBox();
            c.setSelected(false);
            checkBoxes.add(c);
            BBPanel.add(c);
            
        }
        this.setUpMidi();
        
        frame.setBounds(50, 50, 500, 500);
        //frame.setSize(800, 800);
        frame.pack();
        frame.setVisible(true);
        //frame.setResizable(false);
        
    }
    
    public void setUpMidi(){
        try{
            sequencer = MidiSystem.getSequencer();
            sequencer.open();
            sequence = new Sequence(Sequence.PPQ, 4);
            gaana = sequence.createTrack();
            sequencer.setTempoInBPM(120);
            int[] eventsIWant ={127};
            sequencer.addControllerEventListener(drawPanel , eventsIWant);
        }catch(Exception e){
             e.printStackTrace();
        }
    }
    public void buildAndStartTrack(){
        int[] trackList = null;
        try{
            sequence.deleteTrack(gaana); //deletes Previous Track
            gaana = sequence.createTrack();
            trackList = new int[16];
        for(int i = 0; i<16; i++){
            
             int key = instruments[i];
             for(int j = 0; j<16; j++){
                 JCheckBox jc = (JCheckBox) checkBoxes.get(j + 16*i);
                
                 if(jc.isSelected()){
                     //add instrument 'i' at beat 'j'
                     trackList[j] = key;
                 }
                 else{
                     trackList[j]= 0;
                 }
             }
             makeTrack(trackList);
             
             //gaana.add(makeEvent(176, 1, 127, 0, 16));
        }
        gaana.add(makeEvent(192, 9, 1, 0, 15));//we always want to make sure 
        //that there is an event at beat 16 Otherwise, the sequencer misght not
        //go upto 16 beats before starting over
        }catch(NullPointerException e){
            System.out.println("sequencer na bn raha");
            e.printStackTrace();
        }
        try{
            sequencer.setSequence(sequence);
            sequencer.setLoopCount(Sequencer.LOOP_CONTINUOUSLY);
            sequencer.start();
            sequencer.setTempoInBPM(120);
            
        }catch(Exception e){
            System.out.println("Gaana nhi add hua");
            e.printStackTrace();
        }
    }//close method buildAndStartTrack
    public class startListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent ev){
            buildAndStartTrack();
        }
    }//closing inner class
    public class stopListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent ev){
            sequencer.stop();
        }
    }//close
    public class upTempoListener implements ActionListener{
        public void actionPerformed(ActionEvent ev){
             float tempoFactor = sequencer.getTempoFactor();
             sequencer.setTempoFactor((float) (tempoFactor*1.03));
        }
    }
    public class downTempoListener implements ActionListener{
        public void actionPerformed(ActionEvent ev){
             float tempoFactor = sequencer.getTempoFactor();
             sequencer.setTempoFactor((float)(tempoFactor*.97));
        }
    }
    public class saveGaanaListener implements ActionListener{
        public void actionPerformed(ActionEvent ev){
            boolean[] checkBoxState = new boolean[256];
            for(int i=0; i<256; i++ ){
                JCheckBox jc = (JCheckBox)checkBoxes.get(i);
                if(jc.isSelected())
                    checkBoxState[i] = true;
                else
                    checkBoxState[i] = false;
            }
            
            try{
                JFileChooser fileToSave = new JFileChooser();
                fileToSave.showSaveDialog(frame);
                File f = fileToSave.getSelectedFile();
		if(f.exists()){
                    int response = JOptionPane.showConfirmDialog(null,
                            "File already exist, Do you want to overwrite?",
                            "Confirm",JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE);
                    if(response != JOptionPane.YES_OPTION){
                        return ;
                    }
		}
                ObjectOutputStream os = new ObjectOutputStream(new 
                    FileOutputStream(f));
                os.writeObject(checkBoxState);
                os.close();
              
            }catch(Exception e){
                
            }
        }
    }
    public class loadGaanaListener implements ActionListener{
        public void actionPerformed(ActionEvent ev){
            boolean[] restoreCheckBoxState= null;
            try{
                JFileChooser fileToLoad = new JFileChooser();
                fileToLoad.showOpenDialog(frame);
                ObjectInputStream is = new ObjectInputStream(new
                    FileInputStream(fileToLoad.getSelectedFile()));
                restoreCheckBoxState = (boolean[])is.readObject();
              
            }catch(Exception e){
                JOptionPane.showMessageDialog(null,"You didn't choose any file");
            }
            if(restoreCheckBoxState != null){
                for(int i =0 ; i<256; i++){
                    JCheckBox jc = (JCheckBox) checkBoxes.get(i);
                    if(restoreCheckBoxState[i])
                        jc.setSelected(true);
                    else
                        jc.setSelected(false);
                }
                sequencer.stop();
                buildAndStartTrack();
            }
        }
    }
    public class clearCheckBoxListener implements ActionListener{
        public void actionPerformed(ActionEvent ev){
            for(int i =0; i<256; i++){
                JCheckBox jc = (JCheckBox) checkBoxes.get(i);
                jc.setSelected(false);
            }
        }
    }
    public void makeTrack(int[] list){
        for(int i =0; i<16; i++){
            int key = list[i];
            if(key!=0){
                gaana.add(makeEvent(144, 9, key, 100, i));
                gaana.add(makeEvent(176, 9, 127, 0, i));
                gaana.add(makeEvent(128, 9, key, 100, i+1));    
            } 
        }
    }
    public static MidiEvent makeEvent(int comd, int chan, int one, int two, 
            int tick){
		MidiEvent event= null;
		try{
			ShortMessage a = new ShortMessage();
			a.setMessage(comd, chan, one, two);
			event = new MidiEvent(a, tick);
		}catch(Exception e){
                    System.out.println("Galti gana add krne me hui");
                    e.printStackTrace();
                }
		
		return event;
	}
    public class MyDrawPanel extends JPanel implements ControllerEventListener{
		boolean msg = false;
		public void controlChange(ShortMessage s){
			msg = true;
			repaint();
		}
		public void paintComponent(Graphics g){
			if(msg){
				g.setColor(Color.black); 
				g.fillRect(0,0, getWidth(),getHeight());
				Graphics2D g2 = (Graphics2D) g;
				
                                int r = (int)(250* Math.random());
				int gr = (int) (Math.random()*250);
				int b = (int) (Math.random()*250);
				
                                g2.setColor(new Color(r, gr, b));
				
                                int height = (int)(Math.random() *100) +10;
				int width = (int)(Math.random()*100 +10);
				int x = (int)(Math.random()*50 +10);
				int y = (int)(Math.random()*50 +10);
				
                                g2.fillRect(x,y, width, height);
				msg = false;
			}
		}
		
	}
}


