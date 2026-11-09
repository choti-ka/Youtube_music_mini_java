import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

// ===== เก็บข้อมูลเพลง =====
class Song {
    int id;
    String title;
    String artist;

    Song(int id, String title, String artist) {
        this.id = id;
        this.title = title;
        this.artist = artist;
    }
}

// ===== MusicPlayer =====
class MusicPlayer {
    Map<Integer, Song> songs = new LinkedHashMap<>(); // Map หาเพลง
    LinkedList<Song> playlist = new LinkedList<>();   // LinkedList playlist
    Set<Integer> liked = new HashSet<>();             // Set เพลงที่ชอบห้ามซ้ำ
    Queue<Song> queue = new LinkedList<>();           // Queue คิวเพลงถัดไป
    Stack<Song> history = new Stack<>();              // Stack เก้บประวัติ
    Song current = null;                              // เพลงที่กำลังเล่น

    void addSong(int id, String title, String artist) {
        songs.put(id, new Song(id, title, artist));
    }
    // add to playlist
    String addToPlaylist(Song s) {
        if (playlist.contains(s)) {
            return "Already In The Playlist";
        }
        playlist.add(s);
        return "Add To Playlist";
    }
    // queue
    String addToQueue(Song s) {
        queue.add(s);
        return "Add To Queue";
    }
    // เล่นเพลงที่เลือก เก็บเพลงเก่าลง Stack
    void play(Song s) {
        if (current != null) {
            history.push(current);
        }
        current = s;
    }
    // next song
    boolean next() {
        if (queue.isEmpty()) {
            return false;
        }
        play(queue.poll());
        return true;
    }
    // ย้อนกลับ ดึงเพลงบนสุดของ Stack
    boolean previous() {
        if (history.isEmpty()) {
            return false;
        }
        current = history.pop();
        return true;
    }
    // like/unlike
    void toggleLike() {
        if (current == null) {
            return;
        }
        if (liked.contains(current.id)) {
            liked.remove(current.id);
        } else {
            liked.add(current.id);
        }
    }
    // liked list
    List<Song> getLikedSongs() {
        List<Song> result = new ArrayList<>();
        for (int id : liked) {
            result.add(songs.get(id));
        }
        return result;
    }
    // sort by song title
    void sortByTitle() {
        playlist.sort((a, b) -> a.title.compareTo(b.title));
    }
    // สุ่มเพลย์ลิสต์ด้วย Fisher-Yates
    void shuffle() {
        Random random = new Random();
        for (int i = playlist.size() - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            Song temp = playlist.get(i);
            playlist.set(i, playlist.get(j));
            playlist.set(j, temp);
        }
    }
}
// ===== Main menu + gui =====
public class Main extends JFrame {
    // color
    static final Color BG = new Color(18, 18, 18);
    static final Color PANEL = new Color(33, 33, 33);
    static final Color SIDEBAR = new Color(10, 10, 10);
    static final Color RED = new Color(255, 0, 51);
    static final Color GREY = new Color(170, 170, 170);

    MusicPlayer player = new MusicPlayer();
    String view = "all"; // หน้าที่แสดงอยู่

    DefaultListModel<Song> model = new DefaultListModel<>();
    JList<Song> list = new JList<>(model);
    JLabel titleLabel = new JLabel();
    JLabel statusLabel = new JLabel("Double-click to play");
    JLabel nowLabel = new JLabel();
    JTextField searchBox = new JTextField(14);

    Main() {
        // song
        player.addSong(1, "ส่วนผสมลับ (Secret ingredient)", "Jh4y");
        player.addSong(2, "รู้มือ^^", "iluvujayjay");
        player.addSong(3, "Dirtytalk (ร่วมกับ YUNGTARR)", "GUZT");
        player.addSong(4, "Treasure (ร่วมกับ Yungtarr)", "Percy");
        player.addSong(5, "หินหยดลงน้ำ (Adore)", "YENTED");
        player.addSong(6, "สมพรปาก", "Yungtarr");
        player.addSong(7, "ไม่ใช่อิจฉา", "Faye Fang Kaew");
        player.addSong(8, "คนทางนั้น", "Gift My Project");
        player.addSong(9, "หมายความว่าอะไร", "MEAN");
        player.addSong(10, "Love", "พาราด็อกซ์");
        player.addSong(11, "มีอะไรอีกมั้ยที่ลืมบอก (Top Secret)", "Timethai");

        setTitle("Mini YouTube Music");
        setSize(880, 560);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG);

        add(makeSidebar(), BorderLayout.WEST);
        add(makeCenter(), BorderLayout.CENTER);
        add(makePlayerBar(), BorderLayout.SOUTH);

        refresh();
    }

    // ---------- แถบเมนูซ้าย ----------
    JPanel makeSidebar() {
        JPanel side = new JPanel(new BorderLayout());
        side.setBackground(SIDEBAR);
        side.setPreferredSize(new Dimension(170, 0));

        JLabel logo = new JLabel("  Mini Music");
        logo.setForeground(RED);
        logo.setFont(new Font("SansSerif", Font.BOLD, 20));
        logo.setBorder(BorderFactory.createEmptyBorder(20, 5, 20, 5));
        side.add(logo, BorderLayout.NORTH);

        JPanel menu = new JPanel(new GridLayout(0, 1, 0, 4));
        menu.setBackground(SIDEBAR);
        menu.add(makeMenuButton("Home", "all"));
        menu.add(makeMenuButton("Playlist", "playlist"));
        menu.add(makeMenuButton("Queue", "queue"));
        menu.add(makeMenuButton("Liked Song", "liked"));

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(SIDEBAR);
        wrap.add(menu, BorderLayout.NORTH);
        side.add(wrap, BorderLayout.CENTER);
        return side;
    }

    JButton makeMenuButton(String text, String viewName) {
        JButton b = makeButton(text, SIDEBAR);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.addActionListener(e -> {
            view = viewName;
            statusLabel.setText(" ");
            refresh();
        });
        return b;
    }

    // ---------- หัวข้อ + ค้นหา + ปุ่ม + รายการเพลง ----------
    JPanel makeCenter() {
        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(BG);
        center.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));
        // ชื่อหน้า + ช่องค้นหา
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 26));
        searchBox.setBackground(PANEL);
        searchBox.setForeground(Color.WHITE);
        searchBox.setCaretColor(Color.WHITE);
        searchBox.setToolTipText("Search song title or artist");
        searchBox.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                refresh();
            }
        });
        JPanel row1 = new JPanel(new BorderLayout());
        row1.setBackground(BG);
        row1.add(titleLabel, BorderLayout.WEST);
        row1.add(searchBox, BorderLayout.EAST);

        // button
        JButton addPlaylistBtn = makeButton("+ playlist", PANEL);
        JButton addQueueBtn = makeButton("+ queue", PANEL);
        JButton sortBtn = makeButton("Sort A-Z", PANEL);
        JButton shuffleBtn = makeButton("Shuffle", PANEL);

        addPlaylistBtn.addActionListener(e -> {
            Song s = list.getSelectedValue();
            if (s == null) {
                statusLabel.setText("Choose song first!");
            } else {
                statusLabel.setText(player.addToPlaylist(s));
            }
            refresh();
        });
        addQueueBtn.addActionListener(e -> {
            Song s = list.getSelectedValue();
            if (s == null) {
                statusLabel.setText("Choose song first!");
            } else {
                statusLabel.setText(player.addToQueue(s));
            }
            refresh();
        });
        sortBtn.addActionListener(e -> {
            player.sortByTitle();
            view = "playlist";
            statusLabel.setText("Sort playlist by name complete!");
            refresh();
        });
        shuffleBtn.addActionListener(e -> {
            player.shuffle();
            view = "playlist";
            statusLabel.setText("Shuffle complete!");
            refresh();
        });

        statusLabel.setForeground(GREY);
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        row2.setBackground(BG);
        row2.add(addPlaylistBtn);
        row2.add(addQueueBtn);
        row2.add(sortBtn);
        row2.add(shuffleBtn);
        row2.add(statusLabel);

        JPanel top = new JPanel(new GridLayout(2, 1));
        top.setBackground(BG);
        top.add(row1);
        top.add(row2);
        center.add(top, BorderLayout.NORTH);

        // รายการเพลง
        list.setBackground(PANEL);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setCellRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList<?> l, Object value,
                                                          int index, boolean selected, boolean focus) {
                super.getListCellRendererComponent(l, value, index, selected, focus);
                Song s = (Song) value;
                String mark = (s == player.current) ? "● " : "";
                setText(songText(s, mark));
                setFont(new Font("SansSerif", Font.BOLD, 14));
                setBackground(selected ? new Color(70, 70, 70) : PANEL);
                setForeground(Color.WHITE);
                setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
                return this;
            }
        });
        // double click to play
        list.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && list.getSelectedValue() != null) {
                    player.play(list.getSelectedValue());
                    refresh();
                }
            }
        });
        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(PANEL);
        center.add(scroll, BorderLayout.CENTER);
        return center;
    }
    // ---------- เพลงแถบล่าง ----------
    JPanel makePlayerBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(SIDEBAR);
        bar.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        nowLabel.setForeground(Color.WHITE);
        nowLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        JButton prevBtn = makeButton("Rewind", PANEL);
        JButton nextBtn = makeButton("Next", RED);
        JButton likeBtn = makeButton("♥ Like", PANEL);

        prevBtn.addActionListener(e -> {
            if (!player.previous()) {
                statusLabel.setText("No song to rewind to!");
            }
            refresh();
        });
        nextBtn.addActionListener(e -> {
            if (!player.next()) {
                statusLabel.setText("Empty queue!");
            }
            refresh();
        });
        likeBtn.addActionListener(e -> {
            player.toggleLike();
            refresh();
        });
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setBackground(SIDEBAR);
        buttons.add(prevBtn);
        buttons.add(nextBtn);
        buttons.add(likeBtn);
        bar.add(nowLabel, BorderLayout.WEST);
        bar.add(buttons, BorderLayout.EAST);
        return bar;
    }
    // ---------- button template ----------
    JButton makeButton(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        b.setOpaque(true);
        return b;
    }
    String songText(Song s, String mark) {
        String heart = player.liked.contains(s.id) ? "  ♥" : "";
        return mark + s.title + " - " + s.artist + heart;
    }
    // ---------- update ----------
    void refresh() {
        List<Song> source;
        switch (view) {
            case "playlist" -> {
                source = player.playlist;
                titleLabel.setText("Playlist");
            }
            case "queue" -> {
                source = new ArrayList<>(player.queue);
                titleLabel.setText("Queue");
            }
            case "liked" -> {
                source = player.getLikedSongs();
                titleLabel.setText("Liked Song");
            }
            default -> {
                source = new ArrayList<>(player.songs.values());
                titleLabel.setText("Home");
            }
        }

        // search
        String key = searchBox.getText().toLowerCase();
        model.clear();
        for (Song s : source) {
            if (s.title.toLowerCase().contains(key) || s.artist.toLowerCase().contains(key)) {
                model.addElement(s);
            }
        }

        // แถบเล่นเพลงด้านล่าง
        if (player.current == null) {
            nowLabel.setText("ยังไม่ได้เล่นเพลง");
        } else {
            Song s = player.current;
            String heart = player.liked.contains(s.id) ? " ♥" : "";
            nowLabel.setText(songText(player.current, ""));
        }
    }

    public static void main(String[] args) {
        try {
            // ใช้หน้าตามาตรฐานเดียวกันทุกเครื่อง เพื่อให้สีปุ่มแสดงถูกต้อง
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            // ใช้ค่าเริ่มต้นไป
        }
        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }
}