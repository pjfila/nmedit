/* Copyright (C) 2006 Christian Schneider
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
 * Created on Jan 21, 2007
 */
package net.sf.nmedit.jtheme.component.plaf;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.plaf.ComponentUI;

import net.sf.nmedit.jpatch.InvalidDescriptorException;
import net.sf.nmedit.jpatch.MoveOperation;
import net.sf.nmedit.jpatch.PModule;
import net.sf.nmedit.jpatch.PModuleContainer;
import net.sf.nmedit.jpatch.PModuleDescriptor;
import net.sf.nmedit.jpatch.PatchUtils;
import net.sf.nmedit.jpatch.history.History;
import net.sf.nmedit.jtheme.JTContext;
import net.sf.nmedit.jtheme.component.JTModule;
import net.sf.nmedit.jtheme.component.JTModuleContainer;
import net.sf.nmedit.jtheme.component.JTPatch;
import net.sf.nmedit.jtheme.dnd.JTDragDrop;
import net.sf.nmedit.nmutils.Platform;
import net.sf.nmedit.nmutils.swing.NmSwingUtilities;

public class JTModuleContainerUI extends ComponentUI
{
    
    public static final String backgroundKey = "ModuleContainerUI.background";
    public static final String DnDAllowedKey = "ModuleContainerUI.DnDAllowed";
    
    public static final String baseFontKey = "ModuleContainer.font";
    
    protected JTModuleContainer jtc;
    
    public JTModuleContainerUI(JTModuleContainer jtc)
    {
        this.jtc = jtc;
    }

    public static JTModuleContainerUI createUI(JComponent c)
    {
        return new JTModuleContainerUI((JTModuleContainer)c);
    }

    public JTModuleContainer getModuleContainer()
    {
        return jtc;
    }

    public void createPopupMenu(JTModuleContainer mc, MouseEvent e)
    {
        JPopupMenu popup = new JPopupMenu();
        popup.add(new ContainerAction(ContainerAction.DELETE_UNUSED));
        popup.show(mc, e.getX(), e.getY());
    }
    
    protected class ContainerAction extends AbstractAction
    {
        
        /**
         * 
         */
        private static final long serialVersionUID = 7135918324094843867L;
        public static final String DELETE_UNUSED = "delete.unused";

        public ContainerAction(String command)
        {
            if (command == DELETE_UNUSED)
            {
                putValue(NAME, "Delete Unused Modules");
                putValue(ACTION_COMMAND_KEY, command);
                PModuleContainer t = getTarget();
                setEnabled(t != null && PatchUtils.hasUnusedModules(t));
            }
        }
        
        protected PModuleContainer getTarget()
        {
            return getModuleContainer().getModuleContainer();
        }
        
        public void actionPerformed(ActionEvent e)
        {
            if (isEnabled())
            {
                if (getValue(ACTION_COMMAND_KEY)==DELETE_UNUSED)
                {
                    PModuleContainer mc = getTarget();
                    if (mc != null)
                    {
                        History history = mc.getPatch().getHistory();
                        try
                        {
                            if (history != null)
                                history.beginRecord();
                        
                            while (PatchUtils.removeUnusedModules(mc)>0);
                        } 
                        finally
                        {
                            if (history != null)
                                history.endRecord();
                        }
                    }
                }
            }
        }
        
    }
    
    public void installUI(JComponent c) 
    {
        JTModuleContainer jtc = (JTModuleContainer) c;
        JTContext context = jtc.getContext();
        UIDefaults defaults = context.getUIDefaults();
        
        Color background = defaults.getColor(backgroundKey);
        if (background != null)
            c.setBackground(background);
        
        Font baseFont = defaults.getFont(baseFontKey);
        if (baseFont != null)
            c.setFont(baseFont);
        
        // System.out.println(DnDAllowedKey+": "+defaults.getBoolean(DnDAllowedKey));
        
        boolean dndAllowed = defaults.getBoolean(DnDAllowedKey); 
        installEventHandler(jtc, dndAllowed);
    }

    public void uninstallUI(JComponent c) 
    {
        JTModuleContainer jtc = (JTModuleContainer) c;
        //JTContext context = jtc.getContext();
        // UIDefaults defaults = context.getUIDefaults();
        
        uninstallEventHandler(jtc);
    }

    public void update(Graphics g, JComponent c) 
    {
        if (c.isOpaque()) 
        {
            g.setColor(c.getBackground());
            g.fillRect(0, 0, c.getWidth(),c.getHeight());
        }
        paint(g, c);
    }
    
    public void paint(Graphics g, JComponent c) 
    {
        // nothing happens here
    }

    public void paintChildrenHack(Graphics g)
    {
        if (selectBoxActive) {
        	g.setColor(Color.BLUE);
        	Rectangle r = selectRectangle;
        	g.drawRect(r.x, r.y, r.width - 1, r.height - 1);
        }    

        Rectangle box = dndBox;
        if (box == null) return;
        
        if (eventHandler.getSelectionCount()>0)
        {
        	if (transferData == null)
        		return;
            Rectangle bbox = transferData.getBoundingBox();
            g.setColor(Color.BLUE);
            for (JTModule m : transferData.getModules()) {
                g.drawRect(box.x + m.getX() - bbox.x, box.y + m.getY() - bbox.y, 
                		m.getWidth() - 1, m.getHeight() - 1);
            }
        }
    }
    
    private transient Rectangle dndBox;
    private transient Point dndInitialScrollLocation;
	protected EventHandler eventHandler;
	public ModuleTransferDataWrapper transferData;
	public boolean selectBoxActive;
	public Point selectStartPoint;
	public Rectangle selectRectangle;
    
    public void updateDnDBoundingBox(Rectangle box)
    {
    	Rectangle repaint;
        if (dndBox != null)
        {
            repaint = dndBox;
            if (box != null)
                SwingUtilities.computeUnion(box.x, box.y, box.width, box.height, repaint);
        }
        else
        {
            repaint = box;
        }
        
        if (repaint != null)
        {
            /*
            int enlarge = 5;
            repaint.x-= enlarge;
            repaint.y-= enlarge;
            repaint.width+= enlarge*2;
            repaint.height+= enlarge*2;*/
        	getModuleContainer().repaintOverlay(repaint);
        }
        
        if (box == null)
        {
            dndBox = null;
            
            if (dndInitialScrollLocation != null)
            {
                Rectangle r = 
                getModuleContainer()
                .getVisibleRect();

                r.x = dndInitialScrollLocation.x;
                r.y = dndInitialScrollLocation.y;
                
            }
            
            getModuleContainer().setPreferredSize(
                getModuleContainer().computePreferredSize(null)        
            );            
            dndInitialScrollLocation = null;
        }
        else
        {
        	if (dndInitialScrollLocation == null)
            {
                Rectangle r = 
                    getModuleContainer()
                    .getVisibleRect();
                
                dndInitialScrollLocation = r.getLocation();
            }
            
            dndBox = new Rectangle(box);

            if (dndBox.x<0) dndBox.x = 0;
            if (dndBox.y<0) dndBox.y = 0;

            int r = dndBox.x + dndBox.width +140;
            int b = dndBox.y + dndBox.height+140;
            
            if (r>getModuleContainer().getWidth()
            || b>getModuleContainer().getHeight()
            ) 
            {
                getModuleContainer().setPreferredSize(new Dimension(r, b));
            }
            
            //getModuleContainer().scrollRectToVisible(dndBox);
        }

    }
    
    private void installEventHandler(JTModuleContainer jtc, boolean dndAllowed)
    {
        eventHandler = createEventHandler(jtc, dndAllowed);
    }

    private void uninstallEventHandler(JTModuleContainer jtc)
    {
        EventHandler handler = lookupEventHandler(jtc);
        if (handler != null) 
            handler.uninstall();
    }
    
    protected Object EventHandlerKey()
    {
        return "JTModuleContainerDnDHandler";
    }

    protected EventHandler createEventHandler(JTModuleContainer jtc, boolean dndAllowed)
    {
        EventHandler handler = new EventHandler(this, dndAllowed);
        jtc.putClientProperty(EventHandlerKey(), handler);
        return handler;
    }
    
    protected EventHandler lookupEventHandler(JTModuleContainer jtc)
    {
        Object obj = jtc.getClientProperty(EventHandlerKey());
        if (obj != null && (obj instanceof EventHandler))
            return (EventHandler) obj;
        return null;
    }
    
    public static interface ModuleTransferData extends Transferable
    {
        JTModuleContainer getSource();
        JTModule[] getModules();
        Point getDragStartLocation();
        Rectangle getBoundingBox();
        Rectangle getBoundingBox(Rectangle r);
    }
    
    protected static class ModuleTransferDataWrapper implements ModuleTransferData
    {
        private EventHandler delegate;
        private Point dragStartLocation;
        
        public ModuleTransferDataWrapper(EventHandler delegate, Point dragStartLocation)
        {
            this.delegate = delegate;
            this.dragStartLocation = dragStartLocation;
        }

        public Point getDragStartLocation()
        {
            return new Point(dragStartLocation);
        }

        public JTModule[] getModules()
        {
            return delegate.getModules();
        }
        
        private transient Rectangle boundingBox;

        public Rectangle getBoundingBox()
        {
            return getBoundingBox(null);
        }
        
        public Rectangle getBoundingBox(Rectangle r)
        {
            if (boundingBox == null)
            {
                boundingBox = new Rectangle(0,0,0,0);
                
                JTModule[] modules = getModules();
                
                if (modules.length>0)
                {
                    JTModule m = modules[0];
                    boundingBox = m.getBounds(boundingBox);
                    for (int i=modules.length-1;i>=1;i--)
                    {
                        m = modules[i];
                        SwingUtilities.computeUnion(
                                m.getX(), m.getY(), 
                                m.getWidth(), m.getHeight(), 
                                boundingBox);
                    }
                }
            }
            
            if (r == null)
            {
                return new Rectangle(boundingBox);
            }
            else
            {
                r.setRect(boundingBox);
                return r;
            }
        }
        
        public JTModuleContainer getSource()
        {
            return delegate.getSource();
        }

        public ModuleTransferData getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException
        {
            if (!isDataFlavorSupported(flavor))
                throw new UnsupportedFlavorException(flavor);
            if (flavor.equals(JTDragDrop.ModuleSelectionFlavor))
            	return this;
            if (flavor.equals(JTDragDrop.PatchFileFlavor)) {
                // NMData data2 = NMData.sharedInstance();

            	JTModuleContainer jmc = delegate.getModuleContainer();
            	// construct new patch and add modules XXX
            	// System.out.println("patch file " + patch.patchFileString());

            	return null;
            }
            
            return null;
        }

        public DataFlavor[] getTransferDataFlavors()
        {
            DataFlavor[] flavors = {JTDragDrop.ModuleSelectionFlavor, JTDragDrop.PatchFileFlavor};
            return flavors;
        }

        public boolean isDataFlavorSupported(DataFlavor flavor)
        {
	        for (DataFlavor f: getTransferDataFlavors())
	            if (f.equals(flavor))
	                return true;
	        return false;
        }
    }
    
    public static class EventHandler
      implements ContainerListener, 
      DropTargetListener, DragGestureListener, DragSourceListener,
      MouseListener, MouseMotionListener
    {

        private JTModuleContainerUI jtcUI;
        private boolean dndAllowed;

        public EventHandler(JTModuleContainerUI jtcUI, boolean dndAllowed)
        {
            this.dndAllowed = dndAllowed;
            this.jtcUI = jtcUI;
            
            if (jtcUI.getModuleContainer().isDnDAllowed())
                install();
        }
        
        public JTModuleContainer getModuleContainer()
        {
            return jtcUI.getModuleContainer();
        }
        
        public void install()
        {
            JTModuleContainer jtc = getModuleContainer();
            installAtModuleContainer(jtc);
            
            if (dndAllowed)
            {
                for (int i=jtc.getComponentCount()-1;i>=0;i--)
                {
                    Component component = jtc.getComponent(i);
                    if (installsAtChild(component))
                        installAtChild(component);
                }   
            }
        }
        
        protected boolean installsAtChild(Component component)
        {
            return component instanceof JTModule; 
        }
        
        public void uninstall()
        {
            JTModuleContainer jtc = getModuleContainer();
            uninstallAtModuleContainer(jtc);

            if (dndAllowed)
            {
                for (int i=jtc.getComponentCount()-1;i>=0;i--)
                {
                    Component component = jtc.getComponent(i);
                    if (installsAtChild(component))
                        uninstallAtChild(component);
                }
            }
        }

        public void componentAdded(ContainerEvent e)
        {
            Component component = e.getChild();
            if (installsAtChild(component))
                installAtChild(component);
        }

        public void componentRemoved(ContainerEvent e)
        {
            Component component = e.getChild();
            if (installsAtChild(component))
                uninstallAtChild(component);
        }

        protected DropTarget moduleContainerDropTarget;
        protected int dndActions =  DnDConstants.ACTION_COPY_OR_MOVE;
        protected DropTargetListener dropTargetListener;

        protected DropTargetListener createDropTargetListener()
        {
            return this;
        }
        
        protected void installAtModuleContainer(JTModuleContainer jtc)
        {
            jtc.addMouseListener(this);
            jtc.addMouseMotionListener(this);
            if (dndAllowed)
            {
                jtc.addContainerListener(this);
                dropTargetListener = createDropTargetListener();
                moduleContainerDropTarget = new DropTarget(jtc, dndActions, dropTargetListener, true);
            }
        }

        protected void uninstallAtModuleContainer(JTModuleContainer jtc)
        {
            jtc.removeMouseListener(this);
            if (dndAllowed)
            {
                jtc.removeContainerListener(this);
            }
        }
        
        protected void installAtChild(Component component)
        {
            component.addMouseListener(this);
            // JTModule module = (JTModule) component;
            DragSource dragSource = DragSource.getDefaultDragSource();
            dragSource.createDefaultDragGestureRecognizer(component, dndActions, this);
        }

        protected void uninstallAtChild(Component component)
        {
            // TODO JTModule module = (JTModule) component;

            component.removeMouseListener(this);
        }
        
        protected JTModuleContainer getContainer()
        {
            return getModuleContainer();
        }

        protected boolean isMDDropOk(int action, Transferable t)
        {
            return
            (action & DnDConstants.ACTION_COPY) >0
            && getContainer().getModuleContainer() != null
            && JTDragDrop.isModuleDescriptorFlavorSupported(t);
        }
        
        public void dragEnter(DropTargetDragEvent dtde)
        {
        	DataFlavor flavors[] = dtde.getTransferable().getTransferDataFlavors();
        	
        	if (jtcUI.transferData == null) {
        		Transferable t = dtde.getTransferable();
        		if (t.isDataFlavorSupported(JTDragDrop.ModuleSelectionFlavor)) {
					try {
						jtcUI.transferData = (ModuleTransferDataWrapper)t.getTransferData(JTDragDrop.ModuleSelectionFlavor);
						jtcUI.updateDnDBoundingBox(null);
					} catch (Throwable e) {
						jtcUI.transferData = null;
					}
        		} else {
        			System.out.println("falvor not supported");
        		}
        	}


        	if (isMDDropOk(dtde.getDropAction(), dtde.getTransferable())) {
        		dtde.acceptDrag(DnDConstants.ACTION_COPY);
        	} else if (dtde.getTransferable().isDataFlavorSupported(JTDragDrop.ModuleSelectionFlavor)) {
        		dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
        	} else {
        		dtde.rejectDrag();
            }
        }

        public void dragExit(DropTargetEvent dte)
        {
            jtcUI.updateDnDBoundingBox(null);
            jtcUI.transferData = null;
        }

        public void dragOver(DropTargetDragEvent dtde)
        {
        	if (isMDDropOk(dtde.getDropAction(), dtde.getTransferable())) {
                dtde.acceptDrag(DnDConstants.ACTION_COPY);
                return;
            }
            
        	jtcUI.updateScrollPosition(dtde.getLocation());
            
            /*if (dtde.getCurrentDataFlavorsAsList().contains(ModuleDragSource.ModuleInfoFlavor))
            {
                dtde.acceptDrag(DnDConstants.ACTION_COPY);
            }
            else*/
            if (dtde.getCurrentDataFlavorsAsList().contains(JTDragDrop.ModuleSelectionFlavor))
            {
                dtde.acceptDrag(dtde.getDropAction() & (DnDConstants.ACTION_MOVE | DnDConstants.ACTION_COPY));

                ModuleTransferData data;
                try
                {
                    data = (ModuleTransferData) dtde.getTransferable().getTransferData(JTDragDrop.ModuleSelectionFlavor);

                    if (data!=null)
                    {
                        paintDragOver(data, dtde);
                        return ;
                    }
                }
                catch (Throwable e)
                {
                    ;
                }
            }

            jtcUI.updateDnDBoundingBox(null);
            
            dtde.rejectDrag();       
        }

        private transient Rectangle cachedRectangle;
        
        private void paintDragOver(ModuleTransferData data, DropTargetDragEvent dtde)
        {
            Rectangle box = (cachedRectangle = data.getBoundingBox(cachedRectangle));
            
            Point o = data.getDragStartLocation();
            Point p = new Point(dtde.getLocation());
                     
            p.x =p.x-o.x;
            p.y =p.y-o.y;

            box.x += p.x;
            box.y += p.y;

            jtcUI.updateDnDBoundingBox(box);
        }

        public void drop(DropTargetDropEvent dtde)
        {
            jtcUI.updateDnDBoundingBox(null);
            jtcUI.transferData = null;
            
            DataFlavor chosen = null;
            Object data = null;
            
            if (isMDDropOk(dtde.getDropAction(), dtde.getTransferable()))
            {
                PModuleContainer mc = getModuleContainer().getModuleContainer();
                PModuleDescriptor md = JTDragDrop.getModuleDescriptor(dtde.getTransferable());
                if (md == null || mc == null)
                {
                    dtde.rejectDrop();
                    return;
                }
                
                Point l = dtde.getLocation();
                
                PModule module;
                try
                {
                    module = mc.createModule(md);
                    module.setScreenLocation(l.x, l.y);
                }
                catch (InvalidDescriptorException e)
                {
                    e.printStackTrace();
                    dtde.rejectDrop();
                    return;
                }
                mc.add(module);
                // TODO short after dropping a new module and then moving it
                // causes a NullPointerException in the next line
                MoveOperation move = module.getParentComponent().createMoveOperation();
                move.setScreenOffset(0, 0);
                move.add(module);
                move.move();
       
                dtde.acceptDrop(DnDConstants.ACTION_COPY);
         
                // compute dimensions of container
                jtcUI.jtc.updateModuleContainerDimensions();
                dtde.dropComplete(true);
            } else if (dtde.isDataFlavorSupported(JTDragDrop.ModuleSelectionFlavor)
                    && dtde.isLocalTransfer())
            {
                chosen = JTDragDrop.ModuleSelectionFlavor;

                Transferable transfer = dtde.getTransferable();
                try {
                    // Get the data
                    dtde.acceptDrop(dtde.getDropAction() & (DnDConstants.ACTION_MOVE | DnDConstants.ACTION_COPY));
                    data = transfer.getTransferData(chosen);
                } catch (Throwable t) {
                    t.printStackTrace();
                    dtde.dropComplete(false);
                    return;
                }

                if (data!=null && data instanceof ModuleTransferData)
                {
                    // Cast the data and create a nice module.
                    ModuleTransferData tdata = ((ModuleTransferData)data);
                    
                    //Point p = dtde.getLocation();

                    int action = dtde.getDropAction();

                    if ((action&DnDConstants.ACTION_MOVE)!=0)
                    {
                    	MoveOperation op = tdata.getSource().getModuleContainer().createMoveOperation();
                    	op.setDestination(getModuleContainer().getModuleContainer());
                        executeOperationOnSelection(tdata, dtde, op);
                    }
                    else
                    {
                    	MoveOperation op = tdata.getSource().getModuleContainer().createCopyOperation();
                    	// check for shift pressed to create links XXX
                    	op.setDestination(getModuleContainer().getModuleContainer());
                        executeOperationOnSelection(tdata, dtde, op);
                    }

                }
                dtde.dropComplete(true);

            } else {
                dtde.rejectDrop();
                dtde.dropComplete(false);
            }
            jtcUI.updateDnDBoundingBox(null);
        }

        private void executeOperationOnSelection(ModuleTransferData tdata, DropTargetDropEvent dtde, MoveOperation op)
        {
            Point o = tdata.getDragStartLocation();
            Point p = new Point(dtde.getLocation());

            p.x = p.x-o.x;
            p.y = p.y-o.y;
            
            JTModuleContainer jtmc = getModuleContainer();
            PModuleContainer mc = jtmc.getModuleContainer();
            
            JTModule[] modules = tdata.getModules();
            for (JTModule jtmodule: modules) {
                op.add(jtmodule.getModule());
            }
            
            op.setScreenOffset(p.x, p.y);
            
            op.move();
            
            Collection<? extends PModule> moved = op.getMovedModules();
                        
            int maxx = 0;
            int maxy = 0;
            
            for (JTModule jtmodule: NmSwingUtilities.getChildren(JTModule.class, jtmc))
            {
                PModule module = jtmodule.getModule();
                if (moved.contains(module))
                {
                    jtmodule.setLocation(module.getScreenLocation());

                    maxx = Math.max(jtmodule.getX(), maxx)+jtmodule.getWidth();
                    maxy = Math.max(jtmodule.getY(), maxy)+jtmodule.getHeight();
                }
                
            }
            
            jtmc.updateModuleContainerDimensions();
        }

        public void dropActionChanged(DropTargetDragEvent dtde)
        {
            // no op
            if (isMDDropOk(dtde.getDropAction(), dtde.getTransferable()))
            {
                dtde.acceptDrag(DnDConstants.ACTION_COPY);
                return;
            }
            
            if (dtde.getTransferable().isDataFlavorSupported(JTDragDrop.ModuleSelectionFlavor))
            {
                dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
                return;
            }
            
            dtde.rejectDrag();
        }

        protected Set<JTModule> selectionSet = new HashSet<JTModule>();
		
        protected int getSelectionSize()
        {
            return selectionSet.size();
        }

        protected boolean isOnlyThisSelected(Object module)
        {
            return getSelectionSize() == 1 && isInSelection(module);
        }

        protected void selectOnly(JTModule module)
        {
            if (!selectionSet.isEmpty())
            {
                for (JTModule dif : selectionSet.toArray(new JTModule[selectionSet.size()]))
                    if (dif != module)
                        removeSelection(dif);
            }
            
            if (selectionSet.isEmpty())
                addSelection(module);
        }
        
        protected void addSelection(JTModule module)
        {
        	JTPatch patch = getModuleContainer().getPatchContainer();
        	for (JTModuleContainer c : patch.getModuleContainers()) {
        		JTModuleContainerUI cUI = c.getUI();
        		if (cUI != null & cUI != jtcUI && cUI.eventHandler != null) {
        			cUI.eventHandler.clearSelection();
        		}
        	}
            selectionSet.add(module);
            module.setSelected(true);
        }

        protected void removeSelection(JTModule module)
        {
            selectionSet.remove(module);
            module.setSelected(false);
        }

        protected boolean isInSelection(Object module)
        {
            return selectionSet.contains(module);
        }
        
        protected boolean isSelectionEmpty()
        {
            return selectionSet.isEmpty();
        }
        
        protected void clearSelection()
        {
            if (selectionSet.isEmpty())
                return;
            
            for (JTModule module: selectionSet)
                module.setSelected(false);
            
            selectionSet.clear();
        }
        
        public void dragGestureRecognized(DragGestureEvent dge)
        {
        	
            JTModule module = (JTModule) dge.getComponent();
            if (!module.isEnabled()) return;
          
            if (isSelectionEmpty())
            {
                addSelection(module);
            }
            else if (isInSelection(module))
            {
                // thats ok
            }
            else
            {
                if (!isInSelection(module))
                {
                    clearSelection();
                    addSelection(module);
                }
            }

            if (!isSelectionEmpty())
            {
                Component c = dge.getComponent();
                Point dndOrigin = dge.getDragOrigin();
                // if (c instanceof JTModule) // always true
                {
                    dndOrigin = SwingUtilities.convertPoint(c, dndOrigin, getModuleContainer());
                }
                
                jtcUI.transferData = new ModuleTransferDataWrapper(this, dndOrigin);
                dge.startDrag(DragSource.DefaultMoveDrop, jtcUI.transferData, this);
            }
        }
        
        public int getSelectionCount()
        {
            return selectionSet.size();
        }

        public JTModule[] getModules()
        {
            return selectionSet.toArray(new JTModule[selectionSet.size()]);
        }

        public JTModuleContainer getSource()
        {
            return getModuleContainer();
        }

        public Point getDragStartLocation()
        {
            throw new UnsupportedOperationException();
        }

        public void dragDropEnd(DragSourceDropEvent dsde)
        {
            // no op
        }

        public void dragEnter(DragSourceDragEvent dsde)
        {
            // no op
        }

        public void dragExit(DragSourceEvent dse)
        {
        	// no op
        }

        public void dragOver(DragSourceDragEvent dsde)
        {
            // no op
        }

        public void dropActionChanged(DragSourceDragEvent dsde)
        {
            // no op
        }

        public void mouseClickedAtModuleContainer(MouseEvent e)
        {
            if (SwingUtilities.isLeftMouseButton(e) && !jtcUI.selectBoxActive)
                clearSelection();
        }
        
        public void mouseClickedAtModule(MouseEvent e)
        {
        	if (Platform.isPopupTrigger(e))
                return;

        	JTModule module = (JTModule) e.getComponent();
            
            boolean shift = e.isShiftDown();
            if (Platform.isFlavor(Platform.OS.MacOSFlavor)) {
            	// apple key has a different behaviour under osx
            	boolean meta = e.isMetaDown();
            	if (meta && shift)
            		return;
            	
            	if (meta) {
            		if (isInSelection(module)) {
            			removeSelection(module);
            		} else {
            			addSelection(module);
            		}
            		return;
            	}
            } else {
            	boolean ctrl = e.isControlDown();
                if (shift && ctrl) return;
                
            	if (ctrl) { 
            		removeSelection(module);
            		return;
            	}
            }
            
            if (shift)
                addSelection(module);
            else // !(shift||ctrl)
            {
                selectOnly(module);
            }
        }
        
        public void mouseClicked(MouseEvent e)
        {
        }
        
        public void mouseEntered(MouseEvent e)
        {
            // no op
        }

        
        
        public void mouseExited(MouseEvent e)
        {
            // no op
        }

        public void mousePressed(MouseEvent e)
        {
            JTModuleContainer mc = jtcUI.getModuleContainer();
            if (Platform.isPopupTrigger(e) && e.getComponent() == mc)
            {
                jtcUI.createPopupMenu(mc, e);
            }
            
            if (e.getComponent() instanceof JTModule)
                mouseClickedAtModule(e);
        }

        public void mouseReleased(MouseEvent e)
        {
            if (dndAllowed)
            {
                if (e.getComponent() == getModuleContainer())
                    mouseClickedAtModuleContainer(e);
            }

            JTModuleContainer mc = jtcUI.getModuleContainer();
            if (SwingUtilities.isLeftMouseButton(e) && e.getComponent() == mc && jtcUI.selectBoxActive) {
            	jtcUI.selectBoxActive = false;
            	mc.repaint();
            }
        }
        
        public void mouseDragged(MouseEvent e) {
        	jtcUI.updateScrollPosition(e.getPoint());
        
        	JTModuleContainer mc = jtcUI.getModuleContainer();
        	if (SwingUtilities.isLeftMouseButton(e) && e.getComponent() == mc) {
        		if (!jtcUI.selectBoxActive) {
        			startNewSelectionRectangle(e);
        		}
        		
        		updateSelectionRectangle(e);
        	}
		}

        HashSet<JTModule> oldSelection = null;
        

        
        private void startNewSelectionRectangle(MouseEvent e) {
        	JTModuleContainer mc = jtcUI.getModuleContainer();
			jtcUI.selectBoxActive = true;
			oldSelection = new HashSet<JTModule>(selectionSet);
			jtcUI.selectStartPoint = new Point(e.getPoint());
			jtcUI.selectRectangle = new Rectangle(jtcUI.selectStartPoint);
			mc.repaintOverlay(jtcUI.selectRectangle);
        }

        private void updateSelectionRectangle(MouseEvent e) {
			Point point = e.getPoint();
			Rectangle select = jtcUI.selectRectangle;
    		Point start = jtcUI.selectStartPoint;
    		JTModuleContainer mc = jtcUI.getModuleContainer();
        	mc.repaintOverlay(select);
    		int x1, x2, y1, y2;
    		x1 = Math.min(start.x, point.x);
    		x2 = Math.max(start.x, point.x);
    		y1 = Math.min(start.y, point.y);
    		y2 = Math.max(start.y, point.y);
    		select.setRect(x1, y1, x2-x1, y2-y1);
    		mc.repaintOverlay(select);
            boolean shift = e.isShiftDown();
            boolean meta = false;
            boolean ctrl = false;
            
            if (Platform.isFlavor(Platform.OS.MacOSFlavor)) {
            	// apple key has a different behaviour under osx
            	meta = e.isMetaDown();
            	if (meta && shift)
            		return;
            } else {
            	ctrl = e.isControlDown();
                if (shift && ctrl) return;
            }

    		if (!meta && !shift && !ctrl) {
    			clearSelection();
    		}
    		Component[] components = mc.getComponents();
    		for (int i=components.length-1;i>=0;i--)
    		{
    			Component c = components[i];
    			if (c instanceof JTModule)
    			{
    				JTModule mui = (JTModule) c;
    				if (select.intersects(mui.getBounds())) {
    					if (meta) {
    						if (oldSelection.contains(mui)) {
    							removeSelection(mui);
    						} else {
    							addSelection(mui);
    						}
    					} else if (ctrl) {
    						if (oldSelection.contains(mui))
    							removeSelection(mui);
    					} else {
    						addSelection(mui);
    					}
    				} else {
    					if (oldSelection.contains(mui)) {
    						addSelection(mui);
    					} else {
    						removeSelection(mui);
    					}
    				}
    			}
    		}

		}

		public void mouseMoved(MouseEvent e) {
			// TODO Auto-generated method stub
		}
        
    }

	public void updateScrollPosition(Point location) {
		Rectangle visible = getModuleContainer().getVisibleRect();
		Dimension dim = getModuleContainer().getSize();
        int wScroll = Math.min(visible.width / 3, 40);
        int hScroll = Math.min(visible.height / 3, 40);
        // System.out.println("w " + wScroll + " h " + hScroll);
        
        Rectangle scrollTo = new Rectangle(location);
        if (location.x < (visible.x + wScroll))
        	scrollTo.x = Math.max(location.x - wScroll, 0);
        if (location.x > (visible.x + visible.width - wScroll))
        	scrollTo.x = location.x + wScroll;
        scrollTo.x = Math.min(scrollTo.x, dim.width);
        
        if (location.y < (visible.y + hScroll))
        	scrollTo.y = Math.max(location.y - hScroll, 0);
        if (location.y > (visible.y + visible.height - hScroll))
        	scrollTo.y = location.y + hScroll;
        scrollTo.y = Math.min(scrollTo.y, dim.height);
        
        // System.out.println("location " + location.x + " " + location.y + " visible " + visible.x + " " + visible.y + " scrollto " + scrollTo.x + " " + scrollTo.y);
        
        getModuleContainer().scrollRectToVisible(scrollTo);
	}
    
}
