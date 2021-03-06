/* Copyright (C) 2006 Christian Schneider, 2019 Ian Hoogeboom
 * 
 * This file is part of Nomad.
 * 
 * Nomad is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * Nomad is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Nomad; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
/*
 * Created on Nov 23, 2006
 * Updated on Dec 16, 2019
 */
package net.sf.nmedit.nomad.core;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTarget;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.RenderedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.net.URI;
import java.util.Iterator;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import net.sf.nmedit.nmutils.Platform;
import net.sf.nmedit.nmutils.Platform.OS;
import net.sf.nmedit.nmutils.properties.SystemProperties;
import net.sf.nmedit.nmutils.properties.SystemPropertyFactory;
import net.sf.nmedit.nmutils.swing.ApplicationClipboard;
import net.sf.nmedit.nmutils.swing.CopyCutPasteTarget;
import net.sf.nmedit.nmutils.swing.WorkIndicator;
import net.sf.nmedit.nomad.core.NomadLoader.LocaleHandler;
import net.sf.nmedit.nomad.core.helpers.DocumentActionActivator;
import net.sf.nmedit.nomad.core.helpers.RuntimeMenuBuilder;
import net.sf.nmedit.nomad.core.i18n.LocaleConfiguration;
import net.sf.nmedit.nomad.core.jpf.PluginView;
import net.sf.nmedit.nomad.core.jpf.TempDir;
import net.sf.nmedit.nomad.core.menulayout.ActionHandler;
import net.sf.nmedit.nomad.core.menulayout.MenuBuilder;
import net.sf.nmedit.nomad.core.menulayout.MenuLayout;
import net.sf.nmedit.nomad.core.misc.NMUtilities;
import net.sf.nmedit.nomad.core.service.ServiceRegistry;
import net.sf.nmedit.nomad.core.service.fileService.FileService;
import net.sf.nmedit.nomad.core.service.fileService.FileServiceTool;
import net.sf.nmedit.nomad.core.service.initService.InitService;
import net.sf.nmedit.nomad.core.swing.Factory;
import net.sf.nmedit.nomad.core.swing.JDropDownButtonControl;
import net.sf.nmedit.nomad.core.swing.SelectedAction;
import net.sf.nmedit.nomad.core.swing.URIListDropHandler;
import net.sf.nmedit.nomad.core.swing.document.DefaultDocumentManager;
import net.sf.nmedit.nomad.core.swing.document.Document;
import net.sf.nmedit.nomad.core.swing.document.DocumentEvent;
import net.sf.nmedit.nomad.core.swing.document.DocumentListener;
import net.sf.nmedit.nomad.core.swing.explorer.ExplorerTree;
import net.sf.nmedit.nomad.core.swing.explorer.FileExplorerTree;
import net.sf.nmedit.nomad.core.swing.tabs.JTabbedPane2;
import net.sf.nmedit.nomad.core.utils.NomadPropertyFactory;
import net.sf.nmedit.nomad.core.utils.OSXAdapter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.java.plugin.PluginManager;
import org.java.plugin.boot.Boot;

public class Nomad
{

    private static final String PROPERY_WINDOW_BOUNDS = "window.bounds";
    
    private static final String MENU_FILE_OPEN = "nomad.menu.file.open";
    private static final String MENU_FILE_SAVE = "nomad.menu.file.save.save";
    private static final String MENU_FILE_SAVEAS = "nomad.menu.file.save.saveas";
    private static final String MENU_FILE_PROPERTIES = "nomad.menu.file.properties";
    private static final String MENU_FILE_EXPORT = "nomad.menu.file.ie.export";
    private static final String MENU_EDIT_COPY = "nomad.menu.edit.edit.copy";
    private static final String MENU_EDIT_PASTE = "nomad.menu.edit.edit.paste";
    private static final String MENU_EDIT_CUT = "nomad.menu.edit.edit.cut";
    private static final String MENU_DEBUG_GC = "nomad.menu.help.debug.gc";
    
    private static Nomad instance;
    private JFrame mainWindow = null;
    private NomadPlugin pluginInstance;
    private Clipboard clipBoard;
    
    private boolean stopped = false;
    
    private DefaultDocumentManager pageContainer ;
    private MenuLayout menuLayout;
    private MenuBuilder menuBuilder;
    private FileExplorerTree explorerTree;
    private JTabbedPane2 toolPane;
    private JTabbedPane2 synthPane;

    public static final String NOMAD_PROPERTIES="nomad.properties";
    
    protected void storeProperties()
    {
        // store properties
        SystemPropertyFactory factory = 
            SystemPropertyFactory.sharedInstance().getFactory();
        if (factory instanceof NomadPropertyFactory)
        {
            Properties p = ((NomadPropertyFactory)factory).getProperties();
            
            try
            {
            OutputStream out = new BufferedOutputStream(new FileOutputStream(getCorePropertiesFile()));
            p.store(out, "do not edit");
            out.flush();
            out.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
    
    public static File getCorePropertiesFile()
    {
        return new File(TempDir.getBaseDir(), NOMAD_PROPERTIES);
    }
    
    public DefaultDocumentManager getDocumentManager()
    {
        return pageContainer;
    }
    
    public Nomad(NomadPlugin plugin, MenuLayout menuLayout) 
    {
        mainWindow = new JFrame("Nomad 0.4.20191218");
        menuBuilder = new MenuBuilder(menuLayout);
        this.pluginInstance = plugin;
        Nomad.instance = this;
        this.menuLayout = menuLayout;
        
        SystemProperties properties = SystemPropertyFactory.getProperties(Nomad.class);
        
        properties.defineRectangleProperty(PROPERY_WINDOW_BOUNDS, null);
        
        Rectangle bounds = properties.rectangleValue(PROPERY_WINDOW_BOUNDS);
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        if (bounds == null)
        {
            bounds = new Rectangle(0, 0, 640, 480);
            NMUtilities.fitRectangle(bounds, screen);
            NMUtilities.centerRectangle(bounds, screen);
        }
        if (bounds.x<0) bounds.x = 0;
        if (bounds.y<0) bounds.y = 0;
        if (bounds.width<=100) bounds.width = 100;
        if (bounds.height<=100) bounds.height = 100;
        
        mainWindow.setBounds(bounds);
    }
    
    public void pluginsHelp()
    {
        Document d = new PluginView();
        getDocumentManager().add(d);
        getDocumentManager().setSelection(d);
    }
    
    private class DocumentSelectionHandler implements DocumentListener
    {
        public void documentAdded(DocumentEvent e)
        {
            // no op
        }

        public void documentRemoved(DocumentEvent e)
        {
            // no op
        }

        public void documentSelected(DocumentEvent e)
        {
            setMenuForDocument(e.getDocument());
        }
        
        public void setMenuForDocument(Document d)
        {
            boolean saveEnabled = false;
            boolean saveAsEnabled = false;
            boolean propertiesEnabled = false;
            if (d != null)
            {
                Iterator<FileService> iter = ServiceRegistry.getServices(FileService.class);
                while (iter.hasNext() && (!saveEnabled) && (!saveAsEnabled) && (!propertiesEnabled))
                {
                    FileService fs = iter.next();
                    if (!saveEnabled) saveEnabled = fs.isDirectSaveOperationSupported(d);
                    if (!saveAsEnabled) saveAsEnabled = fs.isSaveOperationSupported(d);
                    if (!propertiesEnabled) propertiesEnabled = fs.isEditPropertiesSupported(d);
                }
            }
            menuLayout.getEntry(MENU_FILE_SAVE).setEnabled(saveEnabled);
            menuLayout.getEntry(MENU_FILE_SAVEAS).setEnabled(saveAsEnabled);
            menuLayout.getEntry(MENU_FILE_PROPERTIES).setEnabled(propertiesEnabled);
            menuLayout.getEntry(MENU_FILE_EXPORT).setEnabled(d instanceof Transferable);
        }
        
    }

    protected CopyCutPasteTarget getActiveCopyCutPasteTarget() {
    	Document doc = Nomad.sharedInstance().getDocumentManager().getSelection();
    	if (doc == null)
    		return null;
    	JComponent c = doc.getComponent();
    	if (c instanceof CopyCutPasteTarget) 
    		return (CopyCutPasteTarget)c;
    	else
    		return null;
    }
    
    public void editCopy() {
    	CopyCutPasteTarget target = getActiveCopyCutPasteTarget();
    	if (target == null)
    		return;
    	if (target.canCopy())
    		target.performCopy(clipBoard);
    }
    
    public void editCut() {
    	CopyCutPasteTarget target = getActiveCopyCutPasteTarget();
    	if (target == null)
    		return;
    	if (target.canCut())
    		target.performCut(clipBoard);
    }
    
    public void editPaste() {
    	CopyCutPasteTarget target = getActiveCopyCutPasteTarget();
    	if (target == null)
    		return;
    	if (target.canPaste())
    		target.performPaste(clipBoard);
    }

    public void export()
    {
        Document doc = getDocumentManager().getSelection();
        if (!(doc instanceof Transferable))
            return;
        
        Transferable transferable = (Transferable) doc;
        
        String title = doc.getTitle();
        if (title == null)
            title = "Export";
        else
            title = "Export '"+title+"'";
        
        JComboBox src = new JComboBox(transferable.getTransferDataFlavors());
        src.setRenderer(new DefaultListCellRenderer(){
            /**
             * 
             */
            private static final long serialVersionUID = -4553255745845039428L;

            public Component getListCellRendererComponent(
                JList list,
            Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus)
            {
                String text;
                if (value instanceof DataFlavor)
                {
                    DataFlavor flavor = (DataFlavor) value;
                    String mimeType = flavor.getMimeType();
                    String humanRep = flavor.getHumanPresentableName();
                    String charset = flavor.getParameter("charset");
                    
                    if (mimeType == null)
                        text = "?";
                    else
                    {
                        text = mimeType;
                        int ix = text.indexOf(';');
                        if (ix>=0)
                            text = text.substring(0, ix).trim();
                    }
                    if (charset != null)
                        text+="; charset="+charset;
                    if (humanRep != null)
                        text+=" ("+humanRep+")";
                }
                else
                {
                    text = String.valueOf(value);
                }
                return super.getListCellRendererComponent(list, text, index, isSelected, cellHasFocus);
            }
        });
        
        JComboBox dst = new JComboBox(new Object[]{"File", "Clipboard"});
        
        Object[] msg = {"Source:", doc.getTitle(), 
                "Export as:", src,
                "Export to:", dst};
        Object[] options = {"Ok", "Cancel"};
        
        JOptionPane op = new JOptionPane(
                msg, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null, options);
        
        JDialog dialog = op.createDialog(getWindow(), title);
        dialog.setModal(true);
        dialog.setVisible(true);
        
        boolean ok =  "Ok".equals(op.getValue());
        
        DataFlavor flavor = (DataFlavor) src.getSelectedItem();
        dialog.dispose();
        if(!ok) return;
        
        if (flavor == null)
            return;
        
        if ("Clipboard".equals(dst.getSelectedItem()))
        {
            Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
            cb.setContents(new SelectedTransfer(flavor, transferable), null);
        }
        else
        {
            export(transferable, flavor);
        }
    }

    private static class SelectedTransfer implements Transferable
    {
        
        private DataFlavor selectedFlavor;
        private Transferable delegate;

        public SelectedTransfer(DataFlavor selectedFlavor, Transferable delegate)
        {
            this.selectedFlavor = selectedFlavor;
            this.delegate = delegate;
        }

        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException
        {
            if (!selectedFlavor.match(flavor))
                throw new UnsupportedFlavorException(flavor);
            return delegate.getTransferData(flavor);
        }

        public DataFlavor[] getTransferDataFlavors()
        {
            DataFlavor[] flavors = {selectedFlavor};
            return flavors;
        }

        public boolean isDataFlavorSupported(DataFlavor flavor)
        {
            return selectedFlavor.equals(flavor);
        }
        
    }
    
    private void export(Transferable transferable, DataFlavor flavor)
    {
        File file = getExportFile();
        if (file == null)
            return;
        
        Reader reader;
        try
        {
            reader = flavor.getReaderForText(transferable);
        }
        catch(Exception e)
        {
            reader = null;
        }
        
        if (reader != null)
        {
            try
            {
                FileOutputStream out = new FileOutputStream(file);
                try
                {
                    int data;
                    while ((data=reader.read())!=-1)
                    {
                        out.write(data);
                    }
                    out.flush();
                }
                finally
                {
                    out.close();
                }
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
            return ;
        }
        
        if (DataFlavor.imageFlavor.match(flavor))
        {
            Image image;
            try
            {
                image = (Image) transferable.getTransferData(flavor);
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
            
            try
            {
                ImageIO.write((RenderedImage) image, "png", file);
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
            
        }
        // else report unsupported flavor
    }

    private File getExportFile()
    {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showSaveDialog(mainWindow)==JFileChooser.APPROVE_OPTION)
        {
            return chooser.getSelectedFile();
        }
        return null;
    }
    
    public void fileSave()
    {
        fileSave(false);
    }

    public void fileSaveAs()
    {
        fileSave(true);
    }
    
    private File lastSaveInFolderLocation = null;
    
    public void fileSave(boolean saveAs)
    {
        Document d = pageContainer.getSelection();
        if (d == null) return;
        
        if (!saveAs)
        {
            Iterator<FileService> iter = ServiceRegistry.getServices(FileService.class);
            FileService useService = null;
            while (iter.hasNext())
            {
                FileService fs = iter.next();
                if (fs.isDirectSaveOperationSupported(d))
                {
                    useService = fs;
                    break;
                }
            }
            
            if (useService != null)
            {
                useService.save(d, useService.getAssociatedFile(d));
                return ;
            }
        }
        

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(lastSaveInFolderLocation);
        chooser.setMultiSelectionEnabled(false);

        Iterator<FileService> iter = ServiceRegistry.getServices(FileService.class);
        while (iter.hasNext())
        {
            FileService fs = iter.next();
            
            boolean add = 
                (saveAs && fs.isSaveOperationSupported(d))
                || ((!saveAs)&&fs.isDirectSaveOperationSupported(d));
            
            if (add) {
//                chooser.addChoosableFileFilter(fs.getFileFilter());
        	    // setFileFilter also add's it, otherwise you will get two entries.
                // set the NmFileChooser as default 
                if (fs.getFileFilter().getExtension().contentEquals("pch"))
                    chooser.setFileFilter(fs.getFileFilter());
                else
                    chooser.addChoosableFileFilter(fs.getFileFilter());
                }
        }

        File sfile = d.getFile();
        if (sfile == null && d.getTitle()!=null)
        	sfile = new File(d.getTitle());
        if (sfile != null)
            chooser.setSelectedFile(sfile);
        
        if (!(chooser.showSaveDialog(mainWindow)==JFileChooser.APPROVE_OPTION)) return;
        
        FileService service = FileServiceTool.lookupFileService(chooser);
        
        
        if (service != null)
        {
            File newFile = chooser.getSelectedFile();
            if (newFile == null) return;
            
            if (newFile.exists() && JOptionPane.showConfirmDialog(mainWindow, "Overwrite existing file '"+newFile.getAbsolutePath()
                    +"' ?", "File already exists.", JOptionPane.YES_NO_OPTION)==JOptionPane.NO_OPTION)
                return;

            service.save(d, newFile);
            
            lastSaveInFolderLocation = newFile.getParentFile();
        }
        else
        {
            JOptionPane.showMessageDialog(mainWindow, "Unknown file type.");
        }
    }

    public void fileProperties()
    {
        Document d = pageContainer.getSelection();
        if (d == null) return;

        FileService service = null;
        
        Iterator<FileService> iter = ServiceRegistry.getServices(FileService.class);
        while (iter.hasNext() && service == null)
        {
            FileService fs = iter.next();
            
            if (fs.isEditPropertiesSupported(d))
            {
                service = fs;
            }
        }

        service.editProperties(d);
    }
    
    public void fileOpen()
    {
        // todo keep working directory
        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(true);
        FileServiceTool.addChoosableFileFilters(chooser);
        if (!(chooser.showOpenDialog(mainWindow)==JFileChooser.APPROVE_OPTION))
            return;
        final File[] selected = chooser.getSelectedFiles();
        final FileService service = FileServiceTool.lookupFileService(chooser);
        
        if (service == null)
        {
            JOptionPane.showMessageDialog(mainWindow, "Unknown file type.");
            return;
        }
        Runnable run = new Runnable() 
        {
            public void run()
            {
                for (File file: selected)
                {
                    service.open(file);
                }
            }
        };
        SwingUtilities.invokeLater(WorkIndicator.create(getWindow(), run));
    }
    
    public ExplorerTree getExplorer()
    {
        return explorerTree;
    }
    
    public JTabbedPane2 getSynthTabbedPane()
    {
        return synthPane;
    }
    
    void setupMenu()
    {
        // before menu builder is used
        RuntimeMenuBuilder.buildNewMenuEntries(menuLayout, "Nord Modular patch 3.0", "Nord Modular");

        ResourceBundle localizedMessages = NomadLoader.getResourceBundle();

        menuLayout.getEntry(MENU_FILE_OPEN)
        .addActionListener(new ActionHandler(this, true, "fileOpen"));
        menuLayout.getEntry(MENU_FILE_SAVE)
        .addActionListener(new ActionHandler(this, true, "fileSave"));
        menuLayout.getEntry(MENU_FILE_SAVEAS)
        .addActionListener(new ActionHandler(this, true, "fileSaveAs"));
        menuLayout.getEntry(MENU_FILE_PROPERTIES)
        .addActionListener(new ActionHandler(this, true, "fileProperties"));
        menuLayout.getEntry(MENU_FILE_EXPORT)
        .addActionListener(new ActionHandler(this, true, "export"));
        menuLayout.getEntry(MENU_EDIT_COPY)
        .addActionListener(new ActionHandler(this, true, "editCopy"));
        menuLayout.getEntry(MENU_EDIT_CUT)
        .addActionListener(new ActionHandler(this, true, "editCut"));
        menuLayout.getEntry(MENU_EDIT_PASTE)
        .addActionListener(new ActionHandler(this, true, "editPaste"));
        menuLayout.getEntry("nomad.menu.help.plugins")
        .addActionListener(new ActionHandler(this, true, "pluginsHelp"));
        menuLayout.getEntry(MENU_DEBUG_GC)
        .addActionListener(new ActionHandler(this, true, "debug_gc"));
        
        
        
        /*
        MLEntry mnLang = menuLayout.getEntry("nomad.menu.window.language");
        
        for (int i=0;i<mnLang.size();i++)
        {
            MLEntry lang = mnLang.getEntryAt(i);
            lang.addActionListener(new LocaleSwitcher(lang.getLocalEntryPoint()));
        }*/
        
        menuBuilder.setResourceBundle(localizedMessages);
        LocaleConfiguration.getLocaleConfiguration().addLocaleChangeListener(new LocaleHandler(menuBuilder));
        final JMenuBar mainMenuBar = menuBuilder.createMenuBar("nomad.menu");
        mainWindow.setJMenuBar(mainMenuBar);
        menuBuilder.addActionListener("nomad.menu.file.exit", new ActionHandler(this, true, "handleExit"));


        MenuLayout.disableGhosts(menuLayout);

        DocumentSelectionHandler dsh = new DocumentSelectionHandler();
        pageContainer.addListener(dsh);
        dsh.setMenuForDocument(pageContainer.getSelection());
        new DocumentActionActivator(pageContainer, menuLayout);
        
    }
    
    public void debug_gc()
    {
        SwingUtilities.invokeLater(new Runnable(){public void run(){
            Runtime rt = Runtime.getRuntime();
            System.out.println("garbage collect");
            System.gc();
            System.out.println("gc: free:"+rt.freeMemory()+" byte, max:"+rt.maxMemory());
        }});
    }
    
    void setupUI()
    {
    	this.clipBoard = new Clipboard("nomad clipboard");
    	ApplicationClipboard.setApplicationClipboard(clipBoard);

        mainWindow.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        mainWindow.addWindowListener(new WindowAdapter() {
                    public void windowClosing( WindowEvent e )
                    {
                        Nomad.sharedInstance().handleExit();
                    }
                });
        pageContainer = new DefaultDocumentManager();

        Container contentPane = mainWindow.getContentPane();
        
        explorerTree = new FileExplorerTree();
        explorerTree.setFont(new Font("Arial", Font.PLAIN, 11));
        
        explorerTree.createPopup(menuBuilder);

        JScrollPane explorerTreeScroller = new JScrollPane(explorerTree);
        toolPane = new JTabbedPane2();
        toolPane.setCloseActionEnabled(false);
        
        toolPane.addTab("Explorer", getImage("/icons/eview16/filenav_nav.gif"), explorerTreeScroller);

        new DropTarget(contentPane, new URIListDropHandler() {
            public void uriListDropped(URI[] uriList)
            {
                for (URI uri: uriList)
                {
                    try
                    { 
                    File f = new File(uri);
                    openOrSelect(f);
                    }
                    catch (IllegalArgumentException e)
                    {
                        // ignore
                    }
                }
            }
        });
        
        synthPane = new JTabbedPane2();
        synthPane.setCloseActionEnabled(true);
        
        JSplitPane sidebarSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, false);
        sidebarSplit.setTopComponent(toolPane);
        sidebarSplit.setBottomComponent(synthPane);
        sidebarSplit.setResizeWeight(0.8);
        sidebarSplit.setOneTouchExpandable(true);
        /*
        JComponent sidebar = new JPanel(new BorderLayout());
        sidebar.setBorder(null);
        sidebar.add(sidebarSplit, BorderLayout.CENTER);
*/
        if (!Platform.isFlavor(OS.MacOSFlavor)) {
            /*
            JToolBar tb = createQuickActionToolbar();
            sidebar.add(tb, BorderLayout.NORTH);*/

        } else {
            registerForMacOSXEvents();
        }
        JSplitPane splitLR = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitLR.setResizeWeight(0);
        splitLR.setDividerLocation(200);
        splitLR.setRightComponent(pageContainer);
        splitLR.setLeftComponent(sidebarSplit);
            
        contentPane.setLayout(new BorderLayout());
        contentPane.add(splitLR, BorderLayout.CENTER);
        if (contentPane instanceof JComponent)
            ((JComponent)contentPane).revalidate();
    }
    
    private JToolBar createQuickActionToolbar()
    {
        JToolBar toolbar = new JToolBar();
        toolbar.setBorderPainted(false);
        toolbar.setFloatable(false);
        toolbar.add(Factory.createToolBarButton(menuLayout.getEntry(MENU_FILE_OPEN)));
        toolbar.addSeparator();
        toolbar.add(Factory.createToolBarButton(menuLayout.getEntry(MENU_FILE_SAVE)));
        toolbar.addSeparator();
        

        JPopupMenu pop = new JPopupMenu();
        Iterator<FileService> iter = ServiceRegistry.getServices(FileService.class);

        JRadioButtonMenuItem rfirst = null;
        SelectedAction sa = new SelectedAction();


        sa.putValue(AbstractAction.SMALL_ICON, getImage("/icons/tango/16x16/actions/document-new.png"));

        while (iter.hasNext())
        {
            FileService fs = iter.next();
            if (fs.isNewFileOperationSupported())
            {
                JRadioButtonMenuItem rb = new JRadioButtonMenuItem(new AHAction(fs.getName(), fs.getIcon(), fs, "newFile"));
                sa.add(rb);
                pop.add(rb);
                if (rfirst == null)
                    rfirst = rb;
            }
        }

        JButton btn = Factory.createToolBarButton(sa);
        toolbar.add(btn);

        new JDropDownButtonControl(btn, pop);
        return toolbar;
    }
    
    public void registerForMacOSXEvents() {
    	try {
    		// Generate and register the OSXAdapter, passing it a hash of all the methods we wish to
    		// use as delegates for various com.apple.eawt.ApplicationListener methods
    		OSXAdapter.setQuitHandler(this, getClass().getDeclaredMethod("handleExit", (Class[])null));
    		OSXAdapter.setFileHandler(this, getClass().getDeclaredMethod("loadOSXFile", new Class[] { String.class }));
    	} catch (Exception e) {
    		System.err.println("Error while loading the OSXAdapter:");
    		e.printStackTrace();
    	}

    }
	
    public void loadOSXFile(String path) {
    	File file = new File(path);
    	FileService service =
    		FileServiceTool.lookupFileService(file);

    	if (service != null)
    	{
    		service.open(file);
    	}
    	else
    	{
    		JOptionPane.showMessageDialog(mainWindow, "Could not find service to open file.");
    	}
    }
    

    
    private static class AHAction extends AbstractAction
    {
        
        /**
         * 
         */
        private static final long serialVersionUID = 3827015254600154428L;
        private ActionHandler actionHandler;
        
        public AHAction(String title, Icon icon, Object imp, String method)
        {
            if (title != null)
                putValue(NAME, title);
            if (icon != null)
                putValue(SMALL_ICON, icon);
            actionHandler = new ActionHandler(imp, method);
        }

        public void actionPerformed(ActionEvent e)
        {
            actionHandler.actionPerformed(e);
        }
        
    }
    
    private ImageIcon getImage(String name)
    {
        return new ImageIcon(getClass().getResource(name));
    }

    public JTabbedPane2 getToolPane()
    {
        return toolPane;
    }
    
    public NomadPlugin getCorePlugin()
    {
        return pluginInstance;
    }
    
    public PluginManager getPluginManager()
    {
        return PluginManager.lookup(pluginInstance);
    }
    
    public static synchronized Nomad sharedInstance()
    {
        return instance;
    }

    public JFrame getWindow()
    {
        return mainWindow;
    }
    
    public boolean askStopApplication()
    {
        if (!stopped)
        {
            for (Document d: getDocumentManager().getDocuments())
            {
                if (d.isModified()) {
                	Nomad n = Nomad.sharedInstance();
                	int result = JOptionPane.showConfirmDialog(n.getWindow().getRootPane(), "Are you sure you want to quit without saving " + d.getTitle() + " ?\nChanges will not be lost upon quit, as the patch will be saved in the current session.",
                			"", JOptionPane.OK_CANCEL_OPTION);
                	if (result != JOptionPane.OK_OPTION)
                		return false;
                	
                }
            }
            stop();
            return stopped;
        }
        return false;
    }
    
    private void stop()
    {
        stopped = true;
        getWindow().setVisible(false);

        SystemProperties properties = SystemPropertyFactory.getProperties(getClass());
        
        properties.setRectangleValue(PROPERY_WINDOW_BOUNDS, mainWindow.getBounds());
        
        for (Iterator<InitService> i=ServiceRegistry.getServices(InitService.class); i.hasNext();)
        {
            try
            {
                i.next().shutdown();
            }
            catch (Throwable t)
            {
                t.printStackTrace();
            }
        }
        
        storeProperties();
        
        shutDownPlugin();
        getPluginManager().shutdown();
        System.exit(0);
    }
    
    private void shutDownPlugin()
    {
        Log log = LogFactory.getLog(getClass());
        log.info("Shutting down "+pluginInstance);
        
        try
        {
            Boot.stopApplication(pluginInstance);
        }
        catch (Exception e)
        {
          //  log.warn(e);
            e.printStackTrace();
        }
    }
    
    public boolean handleExit()
    {
        return askStopApplication();
    }

    public MenuLayout getMenuLayout()
    {
        return menuLayout;
    }
    
    public MenuBuilder getMenuBuilder()
    {
        return menuBuilder;
    }

    public void openOrSelect(File file)
    {
        if (file.isDirectory())
            return;
        
        // find out if file is already open
        
        for (Document d: getDocumentManager().getDocuments())
        {
            if (file.equals(d.getFile()))
            {
                getDocumentManager().setSelection(d);
                return ;
            }
        }
        
        
        Iterator<FileService> iter = ServiceRegistry.getServices(FileService.class);
        while (iter.hasNext())
        {
            FileService fs = iter.next();
            if (fs.isOpenFileOperationSupported())
            {
                if (fs.getFileFilter().accept(file))
                {
                    int count = pageContainer.getDocumentCount();
                    
                    fs.open(file);
                    int newCount = pageContainer.getDocumentCount(); 
                    if (newCount>count)
                    {
                        // condition may be false if fs.open() creates the document
                        // on the event dispatch thread
                        pageContainer.setSelectedIndex(newCount-1);
                    }
                    return;
                }
            }
        }
    }

	public Document setSelectedDocumentByFile(File file) 
	{
		if (file == null) return null;
		
		for (Document d: pageContainer.getDocuments())
		{
			if (file.equals(d.getFile()))
			{
				pageContainer.setSelection(d);
				return d;
			}
		}
		return null;
	}
    
}
