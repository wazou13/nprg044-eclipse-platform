/**
 * 
 */
package cz.cuni.mff.d3s.nprg044.twitter.ui.view;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

import cz.cuni.mff.d3s.nprg044.twitter.ui.view.internal.model.UserNode;
import cz.cuni.mff.d3s.nprg044.twitter.ui.view.providers.MessageTimelineContentProvider;
import cz.cuni.mff.d3s.nprg044.twitter.ui.view.providers.MessageTimelineLabelProvider;

/**
 * @author Michal Malohlava
 *
 */
public class TwitterMessageTimelineView extends ViewPart implements ISelectionListener {
	
	public static final String ID = "cz.cuni.mff.d3s.nprg044.twitter.ui.view.MessageTimelineView";
	
	private static final String[] COLUMN_NAMES = {"#", "username", "message"};
	private static final int[] COLUMN_WIDTHS = {80, 100, 200};
	
	private TableViewer viewer;
	private Text searchBox;
	private ProgressBar progressBar;

	/**
	 * 
	 */
	public TwitterMessageTimelineView() {	
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		GridLayout layout = new GridLayout(1, true);
		parent.setLayout(layout);		
		searchBox = new Text(parent, SWT.SINGLE | SWT.SEARCH | SWT.ICON_SEARCH);
		searchBox.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		searchBox.setText(""); // "vtipy", "novinky", "nyt"
		searchBox.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {				
				if (viewer.getInput() != searchBox) {
					viewer.setInput(searchBox);
				}
			}
		});
		
		progressBar = new ProgressBar(parent, SWT.HORIZONTAL);
		progressBar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				
		viewer = new TableViewer(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		createColumns(viewer);
		viewer.setContentProvider(new MessageTimelineContentProvider(progressBar));
		viewer.setLabelProvider(new MessageTimelineLabelProvider());
//		viewer.setSorter(new NameSorter());
		// the input for the content provider
		viewer.setInput(searchBox);		
		viewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));

		// tuning of the underlying table		
		viewer.getTable().setLinesVisible(true);		
		viewer.getTable().setHeaderVisible(true);
		
		// make selection available to others
		getSite().setSelectionProvider(viewer);
		
		// register this class as a selection consumer
//		getSite().getWorkbenchWindow().getSelectionService().addSelectionListener(this);
		// register this class as a selection consumer on a given widget
		getSite().getWorkbenchWindow().getSelectionService().addSelectionListener(UserViewPart.ID, this);
		
		// we need to register context menu first to allow contribution via extension points
		createContextMenu();
	}
	
	private void createContextMenu() {
		MenuManager menuManager = new MenuManager("#PopupMenu");
		menuManager.setRemoveAllWhenShown(true);
		menuManager.addMenuListener(new IMenuListener() {
			
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				fillContextMenu(manager);				
			}
		});
		Menu menu = menuManager.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuManager, viewer);
	}
	
	private void fillContextMenu(IMenuManager manager) {
		// this is required to allow contribution into the context menu
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));				
	}

	private void createColumns(TableViewer tableViewer) {
		for (int i = 0; i < COLUMN_NAMES.length; i++) {
			TableViewerColumn tvColumn = new TableViewerColumn(tableViewer, SWT.NULL);
			TableColumn column = tvColumn.getColumn();
			column.setWidth(COLUMN_WIDTHS[i]);
			column.setText(COLUMN_NAMES[i]);
			// I can also here register separated cell providers via calling TableViewerColumn.setLabelProvider
			// - @see CellLabelProvider or StyledCellLabelProvider 			
		}		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		this.searchBox.setFocus();
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (!selection.isEmpty() && selection instanceof IStructuredSelection) {
			Object o = ((IStructuredSelection) selection).getFirstElement();
			if (o instanceof UserNode) {
				viewer.setInput(o);
				if (!searchBox.isDisposed()) {
					searchBox.setText(((UserNode) o).getScreenName());
				}
			}
		} else {
			viewer.setInput(searchBox);
		}				
	}
	
	@Override
	public void dispose() {		
		super.dispose();
		// unregister the listeners
		getSite().getWorkbenchWindow().getSelectionService().removeSelectionListener(this);
	}
	
	public void cleanTimeline() {
		searchBox.setText("");
		viewer.refresh();
	}
}
