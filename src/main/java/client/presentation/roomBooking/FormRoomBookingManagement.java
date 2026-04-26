package client.presentation.roomBooking;

import client.network.socket.HotelClient;
import client.network.socket.SocketRequestExecutor;
import client.network.socket.SocketSessionManager;
import client.presentation.core.ServiceRegistry;
import client.presentation.core.SessionContext;
import client.presentation.login.main.Application;
import client.presentation.room.FormUpdateServiceToRoom;
import common.dto.OdrInfoDTO;
import common.dto.RecommendOptionDTO;
import common.dto.RecommendRequestDTO;
import common.dto.RoomDTO;
import common.dto.request_dto.OdrIdRequestDTO;
import common.dto.request_dto.RoomIdRequestDTO;
import common.protocol.command.CommandType;
import common.protocol.request.BaseRequest;
import common.protocol.response.BaseResponse;
import net.miginfocom.swing.MigLayout;
import server.core.service.RoomService;
import server.core.service.RoomStayService;
import server.core.service.ServiceService;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class FormRoomBookingManagement extends JPanel {

    private final Color BG = new Color(0x0B1F33);
    private final Color FG = new Color(0xE9EEF6);
    private final Color PRIMARY = new Color(0xF5C452);
    private final Color HOVER = new Color(0xFFD36E);
    private final Color CARD_BG = new Color(0x102D4A);
    private final Color BORDER = new Color(0x274A6B);

    private final SessionContext session;
    private final RoomService roomService;
    private final RoomStayService roomStayService;
    private final ServiceService serviceService;

    private JTextField txtSearch;
    private JComboBox<String> cbxType, cbxStatus, cbxView;
    private JSpinner spAdults, spChildren;
    private JButton btnSuggest, btnMultiBook, btnCalendar;

    private JPanel pnGrid;
    private JScrollPane scrollPane;

    private String currentStatusFilter = "Trống";
    private final Set<String> selectedRoomIDs = new LinkedHashSet<>();

    private JDialog multiBookDialog;
    private JDialog dialogSuggestRoom;
    private JDialog calendarDialog;

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public FormRoomBookingManagement(SessionContext session, RoomService roomService, RoomStayService roomStayService, ServiceService serviceService) {
        this.session = session;
        this.roomService = roomService;
        this.roomStayService = roomStayService;
        this.serviceService = serviceService;

        setLayout(new BorderLayout());
        setBackground(BG);

        JPanel pnTop = new JPanel(new MigLayout(
                "insets 12 16 12 16, fillx",
                "[grow,fill]",
                "[]8[]"
        ));
        pnTop.setBackground(BG);
        add(pnTop, BorderLayout.NORTH);

        JPanel pnFilter = new JPanel(new MigLayout(
                "fillx, insets 0",
                "[]12[]12[]12[]12[]12[]12[]",
                "[]"
        ));
        pnFilter.setOpaque(false);

        txtSearch = new JTextField();
        txtSearch.setBackground(CARD_BG);
        txtSearch.setForeground(FG);
        txtSearch.setCaretColor(FG);
        txtSearch.setBorder(BorderFactory.createLineBorder(BORDER, 2));
        txtSearch.putClientProperty("JTextField.placeholderText", "Tìm theo RoomID…");
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        pnFilter.add(txtSearch, "wmin 190, growx");

        pnFilter.add(makeFilterLabel("Loại phòng:"), "right");
        cbxType = new JComboBox<>(new String[]{"All", "Phòng đơn", "Phòng đôi"});
        styleCombo(cbxType);
        pnFilter.add(cbxType, "wmin 120");

        pnFilter.add(makeFilterLabel("Trạng thái:"), "right");
        cbxStatus = new JComboBox<>(new String[]{"Trống", "Đặt", "Check-in"});
        styleCombo(cbxStatus);
        pnFilter.add(cbxStatus, "wmin 110");

        pnFilter.add(makeFilterLabel("View:"), "right");
        cbxView = new JComboBox<>(new String[]{"All", "Ban công", "Vườn", "Thành phố", "Biển"});
        styleCombo(cbxView);
        pnFilter.add(cbxView, "wmin 120");

        pnFilter.add(makeFilterLabel("Người lớn:"), "right");
        spAdults = new JSpinner(new SpinnerNumberModel(2, 1, 50, 1));
        pnFilter.add(spAdults, "wmin 55");

        pnFilter.add(makeFilterLabel("Trẻ em:"), "right");
        spChildren = new JSpinner(new SpinnerNumberModel(0, 0, 50, 1));
        pnFilter.add(spChildren, "wmin 55");

        pnTop.add(pnFilter, "growx");

        JPanel pnAction = new JPanel(new MigLayout(
                "insets 0, fillx",
                "[]8[]8[]",
                "[]"
        ));
        pnAction.setOpaque(false);

        btnMultiBook = createActionButton("ĐẶT NHIỀU PHÒNG (F4)");
        btnMultiBook.setEnabled(false);
        btnMultiBook.addActionListener(e -> openMultiBookDialog());
        pnAction.add(btnMultiBook, "wmin 130");

        btnSuggest = createActionButton("GỢI Ý PHÒNG (F5)");
        btnSuggest.addActionListener(e -> openSuggestDialog());
        pnAction.add(btnSuggest, "wmin 120");

        btnCalendar = createActionButton("LỊCH PHÒNG (F6)");
        btnCalendar.addActionListener(e -> openCalendarDialog());
        pnAction.add(btnCalendar, "wmin 120");

        pnTop.add(pnAction, "right");

        pnGrid = new JPanel(new MigLayout(
                "wrap 2, gap 16, insets 16, fillx",
                "[grow][grow]",
                ""
        ));
        pnGrid.setBackground(BG);

        scrollPane = new JScrollPane(pnGrid);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER, 2));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { loadData(); }
            @Override public void removeUpdate(DocumentEvent e) { loadData(); }
            @Override public void changedUpdate(DocumentEvent e) { loadData(); }
        });

        cbxType.addActionListener(e -> loadData());
        cbxView.addActionListener(e -> loadData());
        cbxStatus.addActionListener(e -> {
            currentStatusFilter = String.valueOf(cbxStatus.getSelectedItem());
            selectedRoomIDs.clear();
            updateMultiButtonEnabled();
            loadData();
        });

        initShortcuts();
        cbxStatus.setSelectedItem("Trống");
        loadData();
    }

    private JButton createActionButton(String text) {
        JButton b = new JButton(text);
        b.setBackground(PRIMARY);
        b.setForeground(new Color(0x0B1F33));
        b.setBorder(BorderFactory.createLineBorder(new Color(0xF1B93A), 1));
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.addMouseListener(new Hoverer(b, PRIMARY, HOVER));
        return b;
    }

    private void initShortcuts() {
        InputMap im = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getActionMap();

        im.put(KeyStroke.getKeyStroke("F4"), "multiBook");
        am.put("multiBook", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                if (btnMultiBook.isEnabled()) btnMultiBook.doClick();
            }
        });

        im.put(KeyStroke.getKeyStroke("F5"), "suggestRooms");
        am.put("suggestRooms", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                if (btnSuggest.isEnabled()) btnSuggest.doClick();
            }
        });

        im.put(KeyStroke.getKeyStroke("F6"), "calendar");
        am.put("calendar", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                if (btnCalendar.isEnabled()) btnCalendar.doClick();
            }
        });
    }

    private void openSuggestDialog() {
        if (dialogSuggestRoom != null && dialogSuggestRoom.isShowing()) {
            dialogSuggestRoom.dispose();
            return;
        }

        int adults = (int) spAdults.getValue();
        int children = (int) spChildren.getValue();
        String view = "All".equals(cbxView.getSelectedItem()) ? null : (String) cbxView.getSelectedItem();

        try {
            RecommendRequestDTO request = new RecommendRequestDTO(adults, children, view, 3);
            BaseResponse response = sendRequest(CommandType.GET_RECOMMEND_ROOMS, request);

            if (!response.isSuccess()) {
                JOptionPane.showMessageDialog(this, response.getMessage());
                return;
            }

            @SuppressWarnings("unchecked")
            List<RecommendOptionDTO> options = (List<RecommendOptionDTO>) response.getData();

            if (options == null || options.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Không có gợi ý phòng phù hợp.");
                return;
            }

            Window owner = SwingUtilities.getWindowAncestor(this);
            dialogSuggestRoom = new FormSuggestDialog(owner, options, view, this::applySuggestedRooms);
            dialogSuggestRoom.setVisible(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openMultiBookDialog() {
        if (multiBookDialog != null && multiBookDialog.isShowing()) {
            multiBookDialog.dispose();
            return;
        }

        String empID = session.getEmployee() != null ? session.getEmployee().getEmployeeId() : null;
        if (empID == null || empID.isBlank()) {
            JOptionPane.showMessageDialog(this, "Không xác định được nhân viên đăng nhập.");
            return;
        }

        if (selectedRoomIDs.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng tick chọn ít nhất 1 phòng.");
            return;
        }

        ArrayList<String> roomsToBook = new ArrayList<>(selectedRoomIDs);

        Window owner = SwingUtilities.getWindowAncestor(this);
        multiBookDialog = new FormMultiBookRooms(
                owner,
                roomsToBook,
                session.getEmployee().getEmployeeId(),
                roomStayService,
                ServiceRegistry.getCustomerService(),
                this
        );
        multiBookDialog.setVisible(true);
    }

    private void applySuggestedRooms(Set<String> ids) {
        if (ids == null || ids.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không có phòng được chọn từ gợi ý.");
            return;
        }

        selectedRoomIDs.clear();
        selectedRoomIDs.addAll(ids);

        cbxStatus.setSelectedItem("Trống");
        currentStatusFilter = "Trống";

        updateMultiButtonEnabled();
        showSuggestedRoomsOnGrid(ids);
    }

    private void showSuggestedRoomsOnGrid(Set<String> ids) {
        if (ids == null || ids.isEmpty()) {
            buildCards(Collections.emptyList());
            return;
        }

        List<RoomDTO> suggestedRooms = new ArrayList<>();
        for (String id : ids) {
            try {
                BaseResponse response = sendRequest(CommandType.GET_ROOM_BY_ID, id);
                if (response.isSuccess() && response.getData() instanceof RoomDTO room) {
                    suggestedRooms.add(room);
                }
            } catch (Exception ignored) {
            }
        }

        buildCards(suggestedRooms);

        if (suggestedRooms.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không có phòng trống hợp lệ để hiển thị.");
        }
    }

    public void loadData() {
        try {
            BaseResponse response = sendRequest(CommandType.GET_ALL_ROOMS, null);
            if (!response.isSuccess()) {
                throw new RuntimeException(response.getMessage());
            }

            @SuppressWarnings("unchecked")
            List<RoomDTO> rooms = (List<RoomDTO>) response.getData();
            if (rooms == null) rooms = new ArrayList<>();

            String keyword = txtSearch.getText().trim().toLowerCase();
            String selectedType = String.valueOf(cbxType.getSelectedItem());
            String selectedView = String.valueOf(cbxView.getSelectedItem());

            List<RoomDTO> filtered = new ArrayList<>();

            for (RoomDTO r : rooms) {
                boolean matchKeyword = keyword.isEmpty()
                        || (r.getRoomId() != null && r.getRoomId().toLowerCase().contains(keyword));

                boolean matchType = "All".equalsIgnoreCase(selectedType)
                        || selectedType.equalsIgnoreCase(r.getRoomTypeName());

                boolean matchView = "All".equalsIgnoreCase(selectedView)
                        || selectedView.equalsIgnoreCase(String.valueOf(r.getView()));

                if (!matchKeyword || !matchType || !matchView) {
                    continue;
                }

                boolean hasPendingBooking = false;
                boolean hasActiveCheckIn = false;

                try {
                    BaseResponse pendingRes = sendRequest(
                            CommandType.GET_PENDING_BOOKINGS_OF_ROOM,
                            new RoomIdRequestDTO(r.getRoomId())
                    );
                    if (pendingRes.isSuccess()) {
                        @SuppressWarnings("unchecked")
                        List<OdrInfoDTO> pendingList = (List<OdrInfoDTO>) pendingRes.getData();
                        hasPendingBooking = pendingList != null && !pendingList.isEmpty();
                    }

                    BaseResponse activeRes = sendRequest(
                            CommandType.GET_ACTIVE_CHECKIN_INFO,
                            new RoomIdRequestDTO(r.getRoomId())
                    );
                    if (activeRes.isSuccess()) {
                        hasActiveCheckIn = activeRes.getData() != null;
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                boolean include = switch (currentStatusFilter) {
                    case "Trống" -> !hasActiveCheckIn;
                    case "Đặt" -> hasPendingBooking;
                    case "Check-in" -> hasActiveCheckIn;
                    default -> true;
                };

                if (include) {
                    filtered.add(r);
                }
            }

            buildCards(filtered);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateMultiButtonEnabled() {
        boolean hasSelection = !selectedRoomIDs.isEmpty();
        btnMultiBook.setEnabled("Trống".equalsIgnoreCase(currentStatusFilter) && hasSelection);
    }

    private void buildCards(List<RoomDTO> rooms) {
        pnGrid.removeAll();
        if (rooms == null || rooms.isEmpty()) {
            JLabel lblEmpty = new JLabel("Không có phòng phù hợp.");
            lblEmpty.setForeground(FG);
            lblEmpty.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            pnGrid.add(lblEmpty, "span 2");
        } else {
            for (RoomDTO r : rooms) {
                pnGrid.add(createRoomCard(r), "growx");
            }
        }
        pnGrid.revalidate();
        pnGrid.repaint();
    }

    private JComponent createRoomCard(RoomDTO room) {
        JPanel pnCard = new JPanel(new MigLayout("wrap, fill, gap 8", "[fill]", "[grow 0][fill]"));
        pnCard.setBackground(CARD_BG);
        pnCard.setBorder(BorderFactory.createLineBorder(BORDER, 2));

        JPanel header = new JPanel(new MigLayout("insets 8 10 0 10, fill", "[]8[grow]"));
        header.setBackground(CARD_BG);

        JCheckBox chk = new JCheckBox();
        chk.setOpaque(false);
        chk.setEnabled(canTickForCurrentFilter(room));
        chk.setSelected(selectedRoomIDs.contains(room.getRoomId()));
        chk.addActionListener(e -> {
            if (chk.isSelected()) selectedRoomIDs.add(room.getRoomId());
            else selectedRoomIDs.remove(room.getRoomId());
            updateMultiButtonEnabled();
        });
        header.add(chk);

        JLabel lblTitle = new JLabel(room.getRoomId());
        lblTitle.setForeground(FG);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.add(lblTitle, "growx");
        pnCard.add(header);

        JPanel body = new JPanel(new MigLayout("insets 8 10 10 10", "[180!]16[grow,fill]", "[][grow]"));
        body.setBackground(CARD_BG);

        JLabel lblImgIcon = new JLabel();
        ImageIcon ic = null;
        try {
            String path = room.getImgRoomSource();
            if (path != null && !path.isBlank()) {
                File f = new File(path);
                if (f.exists()) ic = new ImageIcon(f.getAbsolutePath());
                else {
                    URL u = getClass().getResource(path.startsWith("/") ? path : "/" + path);
                    if (u != null) ic = new ImageIcon(u);
                }
            }
        } catch (Exception ignored) {
        }
        if (ic == null) {
            URL nf = getClass().getResource("/images/404-not-found.jpg");
            if (nf != null) ic = new ImageIcon(nf);
        }
        Image scaled = ic != null ? ic.getImage().getScaledInstance(180, -1, Image.SCALE_SMOOTH) : null;
        lblImgIcon.setIcon(scaled != null ? new ImageIcon(scaled) : null);
        lblImgIcon.setBorder(BorderFactory.createLineBorder(BORDER, 2));
        body.add(lblImgIcon, "cell 0 0, spany 2");

        JPanel info = new JPanel(new MigLayout("wrap, insets 0, gapy 4", "[grow,fill]"));
        info.setOpaque(false);
        info.add(infoSubHeader("Thông tin phòng"));
        info.add(infoLine("Mô tả", room.getDescription()));
        info.add(infoLine("View", room.getView() == null ? "-" : room.getView()));
        info.add(infoLine("Loại phòng", room.getRoomTypeName() == null ? "-" : room.getRoomTypeName()));

        List<OdrInfoDTO> pendingBookings = Collections.emptyList();
        OdrInfoDTO activeCheckIn = null;

        try {
            BaseResponse pendingRes = sendRequest(
                    CommandType.GET_PENDING_BOOKINGS_OF_ROOM,
                    new RoomIdRequestDTO(room.getRoomId())
            );
            if (pendingRes.isSuccess()) {
                @SuppressWarnings("unchecked")
                List<OdrInfoDTO> list = (List<OdrInfoDTO>) pendingRes.getData();
                pendingBookings = list == null ? Collections.emptyList() : list;
            }

            BaseResponse activeRes = sendRequest(
                    CommandType.GET_ACTIVE_CHECKIN_INFO,
                    new RoomIdRequestDTO(room.getRoomId())
            );
            if (activeRes.isSuccess()) {
                activeCheckIn = (OdrInfoDTO) activeRes.getData();
            }
        } catch (Exception ignored) {

        }

        if (activeCheckIn != null) {
            info.add(subtleDivider(), "gaptop 6");
            info.add(infoSubHeader("Khách hàng đang ở"));
            info.add(buildBookingInfoPanel(activeCheckIn, false), "growx");
        }

        if (pendingBookings != null && !pendingBookings.isEmpty()) {
            info.add(subtleDivider(), "gaptop 6");
            info.add(infoSubHeader("Khách hàng đã đặt"));

            info.add(infoLine("Số lượng booking", String.valueOf(pendingBookings.size())));

            OdrInfoDTO first = pendingBookings.get(0);
            String earliestRange =
                    (first.getCheckIn() == null ? "-" : DTF.format(first.getCheckIn())) +
                            " → " +
                            (first.getCheckOut() == null ? "-" : DTF.format(first.getCheckOut()));
            info.add(infoLine("Booking sớm nhất", earliestRange));

            JButton btnViewBookings = new JButton("Xem danh sách khách hàng đã đặt (" + pendingBookings.size() + ")");
            btnViewBookings.setBackground(PRIMARY);
            btnViewBookings.setForeground(new Color(0x0B1F33));
            btnViewBookings.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btnViewBookings.setFocusPainted(false);
            btnViewBookings.setBorder(BorderFactory.createLineBorder(new Color(0xF1B93A), 1));

            List<OdrInfoDTO> finalPendingBookings = pendingBookings;
            btnViewBookings.addActionListener(e -> openBookingListDialog(room, finalPendingBookings));

            info.add(btnViewBookings, "gaptop 6, w 260!");
        }

        body.add(info, "cell 1 0 1 2, grow");
        pnCard.add(body, "growx");

        JPopupMenu menu = new JPopupMenu();
        JMenuItem miBookRoom = new JMenuItem("Đặt phòng");
        JMenuItem miCheckIn = new JMenuItem("Check-in");
        JMenuItem miCheckOut = new JMenuItem("Check-out");
        JMenuItem miExtend = new JMenuItem("Gia hạn phòng");
        JMenuItem miChangeRoom = new JMenuItem("Đổi phòng (trước check-in)");
        JMenuItem miChangeRoomWhileCheckIn = new JMenuItem("Đổi phòng (đã check-in)");
        JMenuItem miCancelRoom = new JMenuItem("Hủy đặt phòng");
        JMenuItem miAddService = new JMenuItem("Thêm dịch vụ cho phòng");

        menu.add(miBookRoom);
        menu.add(miCheckIn);
        menu.add(miCheckOut);
        menu.add(miExtend);
        menu.add(miChangeRoom);
        menu.add(miChangeRoomWhileCheckIn);
        menu.add(miCancelRoom);
        menu.addSeparator();
        menu.add(miAddService);

        switch (currentStatusFilter) {
            case "Trống" -> {
                miBookRoom.setEnabled(true);
                miCheckIn.setEnabled(false);
                miCheckOut.setEnabled(false);
                miExtend.setEnabled(false);
                miChangeRoom.setEnabled(false);
                miChangeRoomWhileCheckIn.setEnabled(false);
                miCancelRoom.setEnabled(false);
                miAddService.setEnabled(false);
            }
            case "Đặt" -> {
                miBookRoom.setEnabled(true);
                miCheckIn.setEnabled(true);
                miCheckOut.setEnabled(false);
                miExtend.setEnabled(true);
                miChangeRoom.setEnabled(true);
                miChangeRoomWhileCheckIn.setEnabled(false);
                miCancelRoom.setEnabled(true);
                miAddService.setEnabled(false);
            }
            case "Check-in" -> {
                miBookRoom.setEnabled(false);
                miCheckIn.setEnabled(false);
                miCheckOut.setEnabled(true);
                miExtend.setEnabled(true);
                miChangeRoom.setEnabled(false);
                miChangeRoomWhileCheckIn.setEnabled(true);
                miCancelRoom.setEnabled(false);
                miAddService.setEnabled(true);
            }
        }

        miBookRoom.addActionListener(e -> {
            FormBookRoom form = new FormBookRoom(
                    room.getRoomId(),
                    session.getEmployee().getEmployeeId(),
                    roomStayService,
                    ServiceRegistry.getCustomerService(),
                    this
            );
            form.setLocationRelativeTo(SwingUtilities.getWindowAncestor(this));
            form.setVisible(true);
        });

        miCheckIn.addActionListener(e -> {
            try {
                BaseResponse response = sendRequest(
                        CommandType.GET_PENDING_BOOKINGS_OF_ROOM,
                        new RoomIdRequestDTO(room.getRoomId())
                );

                if (!response.isSuccess()) {
                    JOptionPane.showMessageDialog(this, response.getMessage());
                    return;
                }

                @SuppressWarnings("unchecked")
                List<OdrInfoDTO> list = (List<OdrInfoDTO>) response.getData();

                if (list == null || list.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Phòng này không còn booking chờ.");
                    return;
                }

                if (list.size() == 1) {
                    BaseResponse checkInRes = sendRequest(
                            CommandType.CHECK_IN_BY_ODR_ID,
                            new OdrIdRequestDTO(list.get(0).getOrderDetailRoomId())
                    );
                    JOptionPane.showMessageDialog(this,
                            checkInRes.isSuccess() ? "Check-in thành công." : checkInRes.getMessage());
                    loadData();
                } else {
                    openBookingListDialog(room, list);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        miCheckOut.addActionListener(e -> {
            List<String> targets = resolveTargetRooms(room.getRoomId());
            doCheckOutRooms(targets);
        });

        miExtend.addActionListener(e -> {
            Window owner = SwingUtilities.getWindowAncestor(this);
            FormExtendRoom form = new FormExtendRoom(room.getRoomId(), roomStayService, this);
            form.setLocationRelativeTo(owner);
            form.setVisible(true);
        });

        miChangeRoom.addActionListener(e -> {
            Window owner = SwingUtilities.getWindowAncestor(this);
            FormChangeRoom form = new FormChangeRoom(owner, room.getRoomId(), roomService, roomStayService, this);
            form.setLocationRelativeTo(owner);
            form.setVisible(true);
        });

        miChangeRoomWhileCheckIn.addActionListener(e -> {
            Window owner = SwingUtilities.getWindowAncestor(this);
            FormChangeRoomWhileCheckIn form = new FormChangeRoomWhileCheckIn(owner, room.getRoomId(), roomService, roomStayService, this);
            form.setLocationRelativeTo(owner);
            form.setVisible(true);
        });

        miCancelRoom.addActionListener(e -> {
            try {
                BaseResponse response = sendRequest(
                        CommandType.GET_PENDING_BOOKINGS_OF_ROOM,
                        new RoomIdRequestDTO(room.getRoomId())
                );

                if (!response.isSuccess()) {
                    JOptionPane.showMessageDialog(this, response.getMessage());
                    return;
                }

                @SuppressWarnings("unchecked")
                List<OdrInfoDTO> list = (List<OdrInfoDTO>) response.getData();

                if (list == null || list.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Phòng này không còn booking chờ.");
                    return;
                }

                openBookingListDialog(room, list);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        miAddService.addActionListener(e -> {
            try {
                BaseResponse response = sendRequest(CommandType.GET_ACTIVE_CHECKIN_INFO, new RoomIdRequestDTO(room.getRoomId()));
                if (!response.isSuccess() || response.getData() == null) {
                    JOptionPane.showMessageDialog(this, "Chức năng chỉ áp dụng cho phòng đang Check-in.");
                    return;
                }

                FormUpdateServiceToRoom dialog = new FormUpdateServiceToRoom(
                        FormRoomBookingManagement.this,
                        room.getRoomId(),
                        roomStayService,
                        serviceService
                );
                dialog.setModal(true);
                dialog.setLocationRelativeTo(SwingUtilities.getWindowAncestor(FormRoomBookingManagement.this));
                dialog.setVisible(true);
                loadData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        MouseAdapter cardMouse = new MouseAdapter() {
            private void showPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    menu.show(e.getComponent(), e.getX(), e.getY());
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                showPopup(e);
                if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 1 && chk.isEnabled()) {
                    if (e.getSource() == chk) return;
                    chk.doClick();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                showPopup(e);
            }
        };

        addCardMouseListener(pnCard, cardMouse, chk);
        return pnCard;
    }

    private BaseResponse sendRequest(CommandType commandType, Object data) {
        return SocketSessionManager.send(BaseRequest.of(commandType, data));
    }

    private JPanel buildBookingInfoPanel(OdrInfoDTO odr, boolean showCancelButton) {
        JPanel p = new JPanel(new MigLayout("insets 8 10 8 10, wrap 2, fillx", "[right]12[grow]", "[][][][][]"));
        p.setBackground(new Color(0x12355A));
        p.setBorder(BorderFactory.createLineBorder(BORDER, 1));

        p.add(makeFilterLabel("Mã KH:"));
        p.add(valueLabel(odr.getCustomerId()));

        p.add(makeFilterLabel("Tên KH:"));
        p.add(valueLabel(odr.getFullName()));

        p.add(makeFilterLabel("SĐT:"));
        p.add(valueLabel(odr.getPhone()));

        p.add(makeFilterLabel("Check-in:"));
        p.add(valueLabel(odr.getCheckIn() == null ? "-" : DTF.format(odr.getCheckIn())));

        p.add(makeFilterLabel("Check-out:"));
        p.add(valueLabel(odr.getCheckOut() == null ? "-" : DTF.format(odr.getCheckOut())));

        if (showCancelButton) {
            JButton btnCancel = new JButton("Hủy đặt");
            btnCancel.setBackground(new Color(0xE74C3C));
            btnCancel.setForeground(Color.WHITE);
            btnCancel.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btnCancel.addActionListener(e -> cancelBookingByOdrId(odr));
            p.add(new JLabel());
            p.add(btnCancel, "right");
        }

        return p;
    }

    private JLabel valueLabel(String value) {
        JLabel lb = new JLabel(value == null || value.isBlank() ? "-" : value);
        lb.setForeground(FG);
        lb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        return lb;
    }

    private void cancelBookingByOdrId(OdrInfoDTO odr) {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Xác nhận hủy booking của khách " + odr.getFullName()
                        + "\n(" + (odr.getCheckIn() == null ? "-" : DTF.format(odr.getCheckIn()))
                        + " → "
                        + (odr.getCheckOut() == null ? "-" : DTF.format(odr.getCheckOut())) + ") ?",
                "Xác nhận hủy đặt",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            BaseResponse response = sendRequest(
                    CommandType.CANCEL_BOOKING_BY_ODR_ID,
                    new OdrIdRequestDTO(odr.getOrderDetailRoomId())
            );
            JOptionPane.showMessageDialog(this,
                    response.isSuccess() ? "Đã hủy booking." : response.getMessage());
            loadData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openBookingListDialog(RoomDTO room, List<OdrInfoDTO> bookings) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        FormRoomPendingBookingsDialog dialog = new FormRoomPendingBookingsDialog(
                owner,
                room.getRoomId(),
                bookings,
                roomStayService,
                this::loadData
        );
        dialog.setVisible(true);
    }

    private JLabel infoSubHeader(String text) {
        JLabel lb = new JLabel(text);
        lb.setForeground(PRIMARY);
        lb.setFont(new Font("Segoe UI", Font.BOLD, 14));
        return lb;
    }

    private JSeparator subtleDivider() {
        JSeparator sep = new JSeparator();
        sep.setForeground(BORDER);
        sep.setBackground(BORDER);
        return sep;
    }

    private void doCheckOutRooms(List<String> targets) {
        if (targets == null || targets.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ít nhất 1 phòng.");
            return;
        }

        int ok = 0;
        List<String> failed = new ArrayList<>();

        for (String roomId : targets) {
            try {
                BaseResponse response = sendRequest(CommandType.CHECK_OUT, new RoomIdRequestDTO(roomId));
                if (response.isSuccess()) ok++;
                else failed.add(roomId);
            } catch (Exception ex) {
                failed.add(roomId + " (" + ex.getMessage() + ")");
            }
        }

        JOptionPane.showMessageDialog(this,
                "Check-out thành công: " + ok +
                        (failed.isEmpty() ? "" : "\nThất bại: " + String.join(", ", failed)));

        selectedRoomIDs.clear();
        loadData();
        updateMultiButtonEnabled();
    }

    private boolean canTickForCurrentFilter(RoomDTO room) {
        if ("Trống".equalsIgnoreCase(currentStatusFilter)) return room.isAvailable();
        if ("Đặt".equalsIgnoreCase(currentStatusFilter)) return true;
        if ("Check-in".equalsIgnoreCase(currentStatusFilter)) return true;
        return false;
    }

    private void addCardMouseListener(Component root, MouseAdapter listener, Component exclude) {
        if (root == null || root == exclude) return;
        root.addMouseListener(listener);

        if (root instanceof Container container) {
            for (Component child : container.getComponents()) {
                addCardMouseListener(child, listener, exclude);
            }
        }
    }

    private void openCalendarDialog() {
        if (calendarDialog != null && calendarDialog.isShowing()) {
            calendarDialog.dispose();
            return;
        }

        Window owner = SwingUtilities.getWindowAncestor(this);
        calendarDialog = new FormCalendarBooking(owner, roomService, roomStayService);
        calendarDialog.setLocationRelativeTo(this);
        calendarDialog.setVisible(true);
    }

    private void styleCombo(JComboBox<?> c) {
        c.setBackground(CARD_BG);
        c.setForeground(FG);
        c.setBorder(BorderFactory.createLineBorder(BORDER, 2));
        c.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    }

    private static class Hoverer extends MouseAdapter {
        private final JButton b;
        private final Color base;
        private final Color hover;

        Hoverer(JButton b, Color base, Color hover) {
            this.b = b;
            this.base = base;
            this.hover = hover;
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            b.setBackground(hover);
            b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        @Override
        public void mouseExited(MouseEvent e) {
            b.setBackground(base);
            b.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
    }

    private JLabel makeFilterLabel(String text) {
        JLabel lb = new JLabel(text);
        lb.setForeground(FG);
        lb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        return lb;
    }

    public void resetFiltersAndReload() {
        txtSearch.setText("");
        cbxType.setSelectedItem("All");
        cbxStatus.setSelectedItem("Trống");
        cbxView.setSelectedItem("All");
        spAdults.setValue(2);
        spChildren.setValue(0);
        selectedRoomIDs.clear();
        loadData();
    }

    public void reload() {
        loadData();
    }

    private JPanel infoLine(String key, String value) {
        JPanel p = new JPanel(new MigLayout("insets 0, fillx", "[right]8[grow,fill]", "[]"));
        p.setOpaque(false);

        JLabel lk = new JLabel(key + ":");
        lk.setForeground(new Color(0xB8C4D4));
        lk.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JLabel lv = new JLabel(value == null || value.isBlank() ? "-" : value);
        lv.setForeground(FG);
        lv.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        p.add(lk, "right");
        p.add(lv, "growx");
        return p;
    }

    private List<String> resolveTargetRooms(String currentRoomId) {
        if (selectedRoomIDs == null || selectedRoomIDs.isEmpty()) {
            return Collections.singletonList(currentRoomId);
        }
        if (selectedRoomIDs.contains(currentRoomId)) {
            return new ArrayList<>(selectedRoomIDs);
        }
        return Collections.singletonList(currentRoomId);
    }
}