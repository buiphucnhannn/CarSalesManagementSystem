package vn.edu.ute.carsalesms.view.component;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import vn.edu.ute.carsalesms.controller.CarManagementController;
import vn.edu.ute.carsalesms.model.dto.BrandCommandRequest;
import vn.edu.ute.carsalesms.model.dto.BrandManagementItem;
import vn.edu.ute.carsalesms.model.dto.CarCommandRequest;
import vn.edu.ute.carsalesms.model.dto.CarLookupItem;
import vn.edu.ute.carsalesms.model.dto.CarManagementItem;
import vn.edu.ute.carsalesms.model.dto.CarManagementMetadata;
import vn.edu.ute.carsalesms.model.dto.CategoryCommandRequest;
import vn.edu.ute.carsalesms.model.dto.CategoryManagementItem;
import vn.edu.ute.carsalesms.model.enums.Status;
import vn.edu.ute.carsalesms.view.theme.UiPalette;

public class CarManagementPanel extends JPanel {

    private final CarManagementController carController;
    private final boolean allowDeactivate;
    private final CarTabPanel carTabPanel;
    private final BrandTabPanel brandTabPanel;
    private final CategoryTabPanel categoryTabPanel;

    public CarManagementPanel(CarManagementController carController, boolean allowDeactivate) {
        this.carController = Objects.requireNonNull(carController, "carController is required");
        this.allowDeactivate = allowDeactivate;

        setLayout(new BorderLayout());
        setOpaque(false);

        carTabPanel = new CarTabPanel();
        brandTabPanel = new BrandTabPanel();
        categoryTabPanel = new CategoryTabPanel();

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Xe", carTabPanel);
        tabbedPane.addTab("Hãng xe", brandTabPanel);
        tabbedPane.addTab("Loại xe", categoryTabPanel);
        tabbedPane.addChangeListener(e -> refreshActiveTab(tabbedPane.getSelectedIndex()));
        add(tabbedPane, BorderLayout.CENTER);
    }

    private void refreshActiveTab(int selectedIndex) {
        switch (selectedIndex) {
            case 0 -> carTabPanel.refreshData();
            case 1 -> brandTabPanel.refreshData();
            case 2 -> categoryTabPanel.refreshData();
            default -> {
            }
        }
    }

    private JButton createActionButton(String title) {
        JButton button = new JButton(title);
        button.setFocusPainted(false);
        button.setBackground(UiPalette.ACTION_BG);
        button.setForeground(UiPalette.ACTION_FG);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiPalette.PRIMARY_BORDER),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        return button;
    }

    private JButton createDangerButton() {
        JButton button = createActionButton("Ngừng KD");
        button.setForeground(UiPalette.DANGER);
        return button;
    }

    private Status parseStatusFilter(String raw) {
        if (raw == null || raw.equalsIgnoreCase("Tất cả")) {
            return null;
        }
        return Status.valueOf(raw);
    }

    private JPanel createTableCard(JTable table) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(UiPalette.SURFACE_BACKGROUND);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiPalette.BORDER_SOFT),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));

        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setGridColor(UiPalette.BORDER_SOFT);
        table.setBackground(UiPalette.TABLE_BACKGROUND);
        table.setSelectionBackground(UiPalette.PRIMARY_SOFT);
        table.setSelectionForeground(UiPalette.TEXT_PRIMARY);
        table.getTableHeader().setBackground(UiPalette.PRIMARY_SOFT);
        table.getTableHeader().setForeground(UiPalette.TEXT_PRIMARY);
        table.getTableHeader().setFont(new Font("Segoe UI Semibold", Font.PLAIN, 13));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        card.add(scrollPane, BorderLayout.CENTER);
        return card;
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    private final class CarTabPanel extends JPanel {

        private static final String[] COLUMNS = {
                "Mã xe", "Tên xe", "Hãng", "Loại", "Chi nhánh", "Giá bán", "Tồn kho", "Trạng thái"
        };

        private final JTextField searchField = new JTextField();
        private final JComboBox<String> statusFilter = new JComboBox<>(new String[]{"Tất cả", "ACTIVE", "INACTIVE"});
        private final DefaultTableModel tableModel;
        private final JTable table;
        private final TableRowSorter<DefaultTableModel> sorter;
        private List<CarManagementItem> rows = new ArrayList<>();
        private CarManagementMetadata metadata = CarManagementMetadata.empty();

        private CarTabPanel() {
            setLayout(new BorderLayout(0, 8));
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

            tableModel = new DefaultTableModel(COLUMNS, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            table = new JTable(tableModel);
            sorter = new TableRowSorter<>(tableModel);
            table.setRowSorter(sorter);
            configureCarColumns();

            add(buildToolbar(), BorderLayout.NORTH);
            add(createTableCard(table), BorderLayout.CENTER);

            reloadMetadata();
            refreshData();
        }

        private JPanel buildToolbar() {
            JPanel panel = new JPanel(new BorderLayout(8, 0));
            panel.setOpaque(false);

            JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            left.setOpaque(false);
            searchField.setPreferredSize(new Dimension(250, 30));

            JButton searchButton = createActionButton("Tìm");
            searchButton.addActionListener(e -> applyQuickFilter());

            statusFilter.setPreferredSize(new Dimension(120, 30));
            statusFilter.addActionListener(e -> refreshData());

            left.add(new JLabel("Tìm kiếm:"));
            left.add(searchField);
            left.add(searchButton);
            left.add(new JLabel("Trạng thái:"));
            left.add(statusFilter);

            JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            right.setOpaque(false);

            JButton refreshButton = createActionButton("Làm mới");
            JButton addButton = createActionButton("Thêm xe");
            JButton editButton = createActionButton("Sửa");
            JButton deactivateButton = createDangerButton();

            refreshButton.addActionListener(e -> refreshData());
            addButton.addActionListener(e -> showEditor(null));
            editButton.addActionListener(e -> selected().ifPresentOrElse(this::showEditor,
                    () -> showInfo("Vui lòng chọn xe cần sửa.")));

            right.add(refreshButton);
            right.add(addButton);
            right.add(editButton);
            if (allowDeactivate) {
                deactivateButton.addActionListener(e -> selected().ifPresentOrElse(this::deactivate,
                        () -> showInfo("Vui lòng chọn xe cần ngừng kinh doanh.")));
                right.add(deactivateButton);
            }

            panel.add(left, BorderLayout.WEST);
            panel.add(right, BorderLayout.EAST);
            return panel;
        }

        private void refreshData() {
            try {
                rows = carController.loadCars(searchField.getText(), parseStatusFilter((String) statusFilter.getSelectedItem()));
                NumberFormat currency = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));
                tableModel.setRowCount(0);
                rows.stream()
                        .map(item -> new Object[]{
                                item.carCode(),
                                item.carName(),
                                item.brandName(),
                                item.categoryName(),
                                item.branchName(),
                                currency.format(item.salePrice()),
                                item.availableQuantity(),
                                item.status().name()
                        })
                        .forEach(tableModel::addRow);
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        }

        private void reloadMetadata() {
            try {
                metadata = carController.loadMetadata();
            } catch (Exception ex) {
                metadata = CarManagementMetadata.empty();
                showError(ex.getMessage());
            }
        }

        private Optional<CarManagementItem> selected() {
            int selectedRow = table.getSelectedRow();
            if (selectedRow < 0) {
                return Optional.empty();
            }
            int modelRow = table.convertRowIndexToModel(selectedRow);
            if (modelRow < 0 || modelRow >= rows.size()) {
                return Optional.empty();
            }
            return Optional.of(rows.get(modelRow));
        }

        private void applyQuickFilter() {
            String keyword = searchField.getText() == null ? "" : searchField.getText().trim();
            if (keyword.isBlank()) {
                sorter.setRowFilter(null);
                return;
            }
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(keyword), 0, 1));
        }

        private void deactivate(CarManagementItem item) {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Xác nhận ngừng kinh doanh xe: " + item.carCode() + "?",
                    "Xác nhận",
                    JOptionPane.YES_NO_OPTION
            );
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
            try {
                carController.deactivateCar(item.id());
                refreshData();
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        }

        private void showEditor(CarManagementItem existing) {
            reloadMetadata();
            CarEditorDialog dialog = new CarEditorDialog(SwingUtilities.getWindowAncestor(this), metadata, existing);
            dialog.setVisible(true);
            dialog.getResult().ifPresent(request -> {
                try {
                    if (existing == null) {
                        carController.createCar(request);
                    } else {
                        carController.updateCar(request);
                    }
                    refreshData();
                } catch (Exception ex) {
                    showError(ex.getMessage());
                }
            });
        }

        private void configureCarColumns() {
            // Widen sale price and remaining stock columns so values are less cramped.
            table.getColumnModel().getColumn(5).setPreferredWidth(140);
            table.getColumnModel().getColumn(6).setPreferredWidth(95);
            table.getColumnModel().getColumn(6).setMinWidth(90);

            DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
            centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
            table.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);
        }
    }

    private final class BrandTabPanel extends JPanel {

        private static final String[] COLUMNS = {"Mã hãng", "Tên hãng", "Quốc gia", "Trạng thái"};

        private final JTextField searchField = new JTextField();
        private final JComboBox<String> statusFilter = new JComboBox<>(new String[]{"Tất cả", "ACTIVE", "INACTIVE"});
        private final DefaultTableModel tableModel;
        private final JTable table;
        private List<BrandManagementItem> rows = new ArrayList<>();

        private BrandTabPanel() {
            setLayout(new BorderLayout(0, 8));
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

            tableModel = new DefaultTableModel(COLUMNS, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            table = new JTable(tableModel);

            add(buildToolbar(), BorderLayout.NORTH);
            add(createTableCard(table), BorderLayout.CENTER);
            refreshData();
        }

        private JPanel buildToolbar() {
            JPanel panel = new JPanel(new BorderLayout(8, 0));
            panel.setOpaque(false);

            JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            left.setOpaque(false);
            searchField.setPreferredSize(new Dimension(250, 30));
            statusFilter.setPreferredSize(new Dimension(120, 30));
            statusFilter.addActionListener(e -> refreshData());

            JButton searchButton = createActionButton("Tìm");
            searchButton.addActionListener(e -> refreshData());

            left.add(new JLabel("Tìm kiếm:"));
            left.add(searchField);
            left.add(searchButton);
            left.add(new JLabel("Trạng thái:"));
            left.add(statusFilter);

            JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            right.setOpaque(false);
            JButton refreshButton = createActionButton("Làm mới");
            JButton addButton = createActionButton("Thêm hãng");
            JButton editButton = createActionButton("Sửa");
            JButton deactivateButton = createDangerButton();

            refreshButton.addActionListener(e -> refreshData());
            addButton.addActionListener(e -> showEditor(null));
            editButton.addActionListener(e -> selected().ifPresentOrElse(this::showEditor,
                    () -> showInfo("Vui lòng chọn hãng xe cần sửa.")));

            right.add(refreshButton);
            right.add(addButton);
            right.add(editButton);
            if (allowDeactivate) {
                deactivateButton.addActionListener(e -> selected().ifPresentOrElse(this::deactivate,
                        () -> showInfo("Vui lòng chọn hãng xe cần ngừng hoạt động.")));
                right.add(deactivateButton);
            }

            panel.add(left, BorderLayout.WEST);
            panel.add(right, BorderLayout.EAST);
            return panel;
        }

        private void refreshData() {
            try {
                rows = carController.loadBrands(searchField.getText(), parseStatusFilter((String) statusFilter.getSelectedItem()));
                tableModel.setRowCount(0);
                rows.stream()
                        .map(item -> new Object[]{item.brandCode(), item.brandName(), item.country(), item.status().name()})
                        .forEach(tableModel::addRow);
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        }

        private Optional<BrandManagementItem> selected() {
            int row = table.getSelectedRow();
            if (row < 0 || row >= rows.size()) {
                return Optional.empty();
            }
            return Optional.of(rows.get(table.convertRowIndexToModel(row)));
        }

        private void showEditor(BrandManagementItem existing) {
            CarManagementMetadata metadata;
            try {
                metadata = carController.loadMetadata();
            } catch (Exception ex) {
                showError(ex.getMessage());
                return;
            }
            BrandEditorDialog dialog = new BrandEditorDialog(SwingUtilities.getWindowAncestor(this), existing, metadata.nextBrandCode());
            dialog.setVisible(true);
            dialog.getResult().ifPresent(request -> {
                try {
                    if (existing == null) {
                        carController.createBrand(request);
                    } else {
                        carController.updateBrand(request);
                    }
                    refreshData();
                } catch (Exception ex) {
                    showError(ex.getMessage());
                }
            });
        }

        private void deactivate(BrandManagementItem item) {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Xác nhận ngừng hoạt động hãng: " + item.brandCode() + "?",
                    "Xác nhận",
                    JOptionPane.YES_NO_OPTION
            );
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
            try {
                carController.deactivateBrand(item.id());
                refreshData();
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        }
    }

    private final class CategoryTabPanel extends JPanel {

        private static final String[] COLUMNS = {"Mã loại", "Tên loại", "Trạng thái"};

        private final JTextField searchField = new JTextField();
        private final JComboBox<String> statusFilter = new JComboBox<>(new String[]{"Tất cả", "ACTIVE", "INACTIVE"});
        private final DefaultTableModel tableModel;
        private final JTable table;
        private List<CategoryManagementItem> rows = new ArrayList<>();

        private CategoryTabPanel() {
            setLayout(new BorderLayout(0, 8));
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

            tableModel = new DefaultTableModel(COLUMNS, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            table = new JTable(tableModel);

            add(buildToolbar(), BorderLayout.NORTH);
            add(createTableCard(table), BorderLayout.CENTER);
            refreshData();
        }

        private JPanel buildToolbar() {
            JPanel panel = new JPanel(new BorderLayout(8, 0));
            panel.setOpaque(false);

            JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            left.setOpaque(false);
            searchField.setPreferredSize(new Dimension(250, 30));
            statusFilter.setPreferredSize(new Dimension(120, 30));
            statusFilter.addActionListener(e -> refreshData());

            JButton searchButton = createActionButton("Tìm");
            searchButton.addActionListener(e -> refreshData());

            left.add(new JLabel("Tìm kiếm:"));
            left.add(searchField);
            left.add(searchButton);
            left.add(new JLabel("Trạng thái:"));
            left.add(statusFilter);

            JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            right.setOpaque(false);
            JButton refreshButton = createActionButton("Làm mới");
            JButton addButton = createActionButton("Thêm loại");
            JButton editButton = createActionButton("Sửa");
            JButton deactivateButton = createDangerButton();

            refreshButton.addActionListener(e -> refreshData());
            addButton.addActionListener(e -> showEditor(null));
            editButton.addActionListener(e -> selected().ifPresentOrElse(this::showEditor,
                    () -> showInfo("Vui lòng chọn loại xe cần sửa.")));

            right.add(refreshButton);
            right.add(addButton);
            right.add(editButton);
            if (allowDeactivate) {
                deactivateButton.addActionListener(e -> selected().ifPresentOrElse(this::deactivate,
                        () -> showInfo("Vui lòng chọn loại xe cần ngừng hoạt động.")));
                right.add(deactivateButton);
            }

            panel.add(left, BorderLayout.WEST);
            panel.add(right, BorderLayout.EAST);
            return panel;
        }

        private void refreshData() {
            try {
                rows = carController.loadCategories(searchField.getText(), parseStatusFilter((String) statusFilter.getSelectedItem()));
                tableModel.setRowCount(0);
                rows.stream()
                        .map(item -> new Object[]{item.categoryCode(), item.categoryName(), item.status().name()})
                        .forEach(tableModel::addRow);
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        }

        private Optional<CategoryManagementItem> selected() {
            int row = table.getSelectedRow();
            if (row < 0 || row >= rows.size()) {
                return Optional.empty();
            }
            return Optional.of(rows.get(table.convertRowIndexToModel(row)));
        }

        private void showEditor(CategoryManagementItem existing) {
            CarManagementMetadata metadata;
            try {
                metadata = carController.loadMetadata();
            } catch (Exception ex) {
                showError(ex.getMessage());
                return;
            }
            CategoryEditorDialog dialog = new CategoryEditorDialog(SwingUtilities.getWindowAncestor(this), existing, metadata.nextCategoryCode());
            dialog.setVisible(true);
            dialog.getResult().ifPresent(request -> {
                try {
                    if (existing == null) {
                        carController.createCategory(request);
                    } else {
                        carController.updateCategory(request);
                    }
                    refreshData();
                } catch (Exception ex) {
                    showError(ex.getMessage());
                }
            });
        }

        private void deactivate(CategoryManagementItem item) {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Xác nhận ngừng hoạt động loại xe: " + item.categoryCode() + "?",
                    "Xác nhận",
                    JOptionPane.YES_NO_OPTION
            );
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
            try {
                carController.deactivateCategory(item.id());
                refreshData();
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        }
    }

    private static final class CarEditorDialog extends JDialog {

        private final JTextField codeField = new JTextField();
        private final JTextField nameField = new JTextField();
        private final JComboBox<CarLookupItem> brandCombo = new JComboBox<>();
        private final JComboBox<CarLookupItem> categoryCombo = new JComboBox<>();
        private final JComboBox<CarLookupItem> branchCombo = new JComboBox<>();
        private final JTextField importPriceField = new JTextField();
        private final JTextField salePriceField = new JTextField();
        private final JTextField quantityField = new JTextField();
        private final JTextField availableQuantityField = new JTextField();
        private final JComboBox<Status> statusCombo = new JComboBox<>(Status.values());

        private CarCommandRequest result;
        private final Long editingId;

        private CarEditorDialog(java.awt.Window owner, CarManagementMetadata metadata, CarManagementItem existing) {
            super(owner, existing == null ? "Thêm xe" : "Sửa xe", ModalityType.APPLICATION_MODAL);
            this.editingId = existing == null ? null : existing.id();
            setResizable(false);
            setLayout(new BorderLayout(0, 8));

            JPanel form = new JPanel(new GridLayout(10, 2, 8, 8));
            form.setBorder(BorderFactory.createEmptyBorder(12, 12, 8, 12));

            metadata.brands().forEach(brandCombo::addItem);
            metadata.categories().forEach(categoryCombo::addItem);
            metadata.branches().forEach(branchCombo::addItem);

            form.add(new JLabel("Mã xe"));
            form.add(codeField);
            form.add(new JLabel("Tên xe"));
            form.add(nameField);
            form.add(new JLabel("Hãng"));
            form.add(brandCombo);
            form.add(new JLabel("Loại xe"));
            form.add(categoryCombo);
            form.add(new JLabel("Chi nhánh"));
            form.add(branchCombo);
            form.add(new JLabel("Giá nhập"));
            form.add(importPriceField);
            form.add(new JLabel("Giá bán"));
            form.add(salePriceField);
            form.add(new JLabel("Số lượng"));
            form.add(quantityField);
            form.add(new JLabel("Khả dụng"));
            form.add(availableQuantityField);
            form.add(new JLabel("Trạng thái"));
            form.add(statusCombo);

            if (existing != null) {
                codeField.setText(existing.carCode());
                codeField.setEditable(true);
                nameField.setText(existing.carName());
                importPriceField.setText(existing.importPrice().toPlainString());
                salePriceField.setText(existing.salePrice().toPlainString());
                quantityField.setText(String.valueOf(existing.quantity()));
                availableQuantityField.setText(String.valueOf(existing.availableQuantity()));
                statusCombo.setSelectedItem(existing.status());
                selectById(brandCombo, existing.brandId());
                selectById(categoryCombo, existing.categoryId());
                selectById(branchCombo, existing.branchId());
            } else {
                codeField.setText(metadata.nextCarCode());
                codeField.setEditable(false);
                statusCombo.setSelectedItem(Status.ACTIVE);
            }

            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
            JButton cancelButton = new JButton("Hủy");
            JButton saveButton = new JButton("Lưu");
            cancelButton.addActionListener(e -> dispose());
            saveButton.addActionListener(e -> onSave());
            actions.add(saveButton);
            actions.add(cancelButton);

            add(form, BorderLayout.CENTER);
            add(actions, BorderLayout.SOUTH);
            pack();
            setMinimumSize(new Dimension(640, 460));
            setSize(getMinimumSize());
            setLocationRelativeTo(owner);
        }

        private void onSave() {
            try {
                CarLookupItem brand = (CarLookupItem) brandCombo.getSelectedItem();
                CarLookupItem category = (CarLookupItem) categoryCombo.getSelectedItem();
                CarLookupItem branch = (CarLookupItem) branchCombo.getSelectedItem();
                result = new CarCommandRequest(
                        editingId,
                        codeField.getText(),
                        nameField.getText(),
                        brand == null ? null : brand.id(),
                        category == null ? null : category.id(),
                        branch == null ? null : branch.id(),
                        new java.math.BigDecimal(importPriceField.getText().trim()),
                        new java.math.BigDecimal(salePriceField.getText().trim()),
                        Integer.parseInt(quantityField.getText().trim()),
                        Integer.parseInt(availableQuantityField.getText().trim()),
                        (Status) statusCombo.getSelectedItem()
                );
                dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Dữ liệu không hợp lệ. Vui lòng kiểm tra lại.", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
            }
        }

        private void selectById(JComboBox<CarLookupItem> comboBox, Long id) {
            if (id == null) {
                return;
            }
            for (int i = 0; i < comboBox.getItemCount(); i++) {
                CarLookupItem item = comboBox.getItemAt(i);
                if (id.equals(item.id())) {
                    comboBox.setSelectedIndex(i);
                    return;
                }
            }
        }

        private Optional<CarCommandRequest> getResult() {
            return Optional.ofNullable(result);
        }
    }

    private static final class BrandEditorDialog extends JDialog {

        private final JTextField codeField = new JTextField();
        private final JTextField nameField = new JTextField();
        private final JTextField countryField = new JTextField();
        private final JComboBox<Status> statusCombo = new JComboBox<>(Status.values());

        private BrandCommandRequest result;
        private final Long editingId;

        private BrandEditorDialog(java.awt.Window owner, BrandManagementItem existing, String nextCode) {
            super(owner, existing == null ? "Thêm hãng xe" : "Sửa hãng xe", ModalityType.APPLICATION_MODAL);
            this.editingId = existing == null ? null : existing.id();
            setResizable(false);
            setLayout(new BorderLayout(0, 8));

            JPanel form = new JPanel(new GridLayout(4, 2, 8, 8));
            form.setBorder(BorderFactory.createEmptyBorder(12, 12, 8, 12));
            form.add(new JLabel("Mã hãng"));
            form.add(codeField);
            form.add(new JLabel("Tên hãng"));
            form.add(nameField);
            form.add(new JLabel("Quốc gia"));
            form.add(countryField);
            form.add(new JLabel("Trạng thái"));
            form.add(statusCombo);

            if (existing != null) {
                codeField.setText(existing.brandCode());
                codeField.setEditable(true);
                nameField.setText(existing.brandName());
                countryField.setText(existing.country());
                statusCombo.setSelectedItem(existing.status());
            } else {
                codeField.setText(nextCode);
                codeField.setEditable(false);
                statusCombo.setSelectedItem(Status.ACTIVE);
            }

            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
            JButton cancelButton = new JButton("Hủy");
            JButton saveButton = new JButton("Lưu");
            cancelButton.addActionListener(e -> dispose());
            saveButton.addActionListener(e -> onSave());
            actions.add(saveButton);
            actions.add(cancelButton);

            add(form, BorderLayout.CENTER);
            add(actions, BorderLayout.SOUTH);
            pack();
            setMinimumSize(new Dimension(520, 260));
            setSize(getMinimumSize());
            setLocationRelativeTo(owner);
        }

        private void onSave() {
            result = new BrandCommandRequest(
                    editingId,
                    codeField.getText(),
                    nameField.getText(),
                    countryField.getText(),
                    (Status) statusCombo.getSelectedItem()
            );
            dispose();
        }

        private Optional<BrandCommandRequest> getResult() {
            return Optional.ofNullable(result);
        }
    }

    private static final class CategoryEditorDialog extends JDialog {

        private final JTextField codeField = new JTextField();
        private final JTextField nameField = new JTextField();
        private final JComboBox<Status> statusCombo = new JComboBox<>(Status.values());

        private CategoryCommandRequest result;
        private final Long editingId;

        private CategoryEditorDialog(java.awt.Window owner, CategoryManagementItem existing, String nextCode) {
            super(owner, existing == null ? "Thêm loại xe" : "Sửa loại xe", ModalityType.APPLICATION_MODAL);
            this.editingId = existing == null ? null : existing.id();
            setResizable(false);
            setLayout(new BorderLayout(0, 8));

            JPanel form = new JPanel(new GridLayout(3, 2, 8, 8));
            form.setBorder(BorderFactory.createEmptyBorder(12, 12, 8, 12));
            form.add(new JLabel("Mã loại"));
            form.add(codeField);
            form.add(new JLabel("Tên loại"));
            form.add(nameField);
            form.add(new JLabel("Trạng thái"));
            form.add(statusCombo);

            if (existing != null) {
                codeField.setText(existing.categoryCode());
                codeField.setEditable(true);
                nameField.setText(existing.categoryName());
                statusCombo.setSelectedItem(existing.status());
            } else {
                codeField.setText(nextCode);
                codeField.setEditable(false);
                statusCombo.setSelectedItem(Status.ACTIVE);
            }

            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
            JButton cancelButton = new JButton("Hủy");
            JButton saveButton = new JButton("Lưu");
            cancelButton.addActionListener(e -> dispose());
            saveButton.addActionListener(e -> onSave());
            actions.add(saveButton);
            actions.add(cancelButton);

            add(form, BorderLayout.CENTER);
            add(actions, BorderLayout.SOUTH);
            pack();
            setMinimumSize(new Dimension(500, 220));
            setSize(getMinimumSize());
            setLocationRelativeTo(owner);
        }

        private void onSave() {
            result = new CategoryCommandRequest(
                    editingId,
                    codeField.getText(),
                    nameField.getText(),
                    (Status) statusCombo.getSelectedItem()
            );
            dispose();
        }

        private Optional<CategoryCommandRequest> getResult() {
            return Optional.ofNullable(result);
        }
    }
}
