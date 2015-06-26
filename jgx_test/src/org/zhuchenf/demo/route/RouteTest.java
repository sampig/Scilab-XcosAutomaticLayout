package org.zhuchenf.demo.route;

import java.util.ArrayList;
import java.util.List;

public class RouteTest {
    boolean bool = true;
    List<Integer[]> minway = null;

    public RouteTest() {
        r(M.sp[0], M.sp[1], new ArrayList<Integer[]>(), copyb(M.a));
        for (Integer[] p : minway) {
            System.out.print("[" + p[0] + "," + p[1] + "]->");
        }
        System.out.println("ep");
    }

    private void r(int x, int y, List<Integer[]> l, Integer[][] b) {
        cb(x, y, b);
        if (x == M.ep[0] && y == M.ep[1]) {
            if (minway == null) {
                minway = l;
            } else {
                if (minway.size() > l.size()) {
                    minway = l;
                }
            }
            return;
        }
        // System.out.println(x+","+y);
        // left
        if (x > 0)
            if (b[x - 1][y] == 0) {
                Integer[] p = { x - 1, y };
                l.add(p);
                r(x - 1, y, copyList(l), copyb(b));
            }
        if (!bool)
            rle(l);
        bool = true;
        // right
        if (x < M.mx)
            if (b[x + 1][y] == 0) {
                Integer[] p = { x + 1, y };
                l.add(p);
                r(x + 1, y, copyList(l), copyb(b));
            }
        if (!bool)
            rle(l);
        bool = true;
        // top
        if (y > 0)
            if (b[x][y - 1] == 0) {
                Integer[] p = { x, y - 1 };
                l.add(p);
                r(x, y - 1, copyList(l), copyb(b));
            }
        if (!bool)
            rle(l);
        bool = true;
        // bottom
        if (y < M.my)
            if (b[x][y + 1] == 0) {
                Integer[] p = { x, y + 1 };
                l.add(p);
                r(x, y + 1, copyList(l), copyb(b));
            }
        bool = false;
        return;
    }

    private List<Integer[]> copyList(List<Integer[]> l) {
        List<Integer[]> l2 = new ArrayList<Integer[]>();
        for (Integer[] e : l)
            l2.add(e);
        return l2;
    }

    private void rle(List<Integer[]> l) {
        int last = l.size();
        if (last > 0)
            l.remove(last - 1);
    }

    public void cb(int x, int y, Integer[][] b) {
        b[x][y] = 1;
    }

    private Integer[][] copyb(Integer[][] a) {
        Integer[][] b = new Integer[M.mx + 1][M.my + 1];
        for (int i = 0; i <= M.mx; i++) {
            for (int j = 0; j <= M.my; j++) {
                b[i][j] = a[i][j];
            }
        }
        return b;
    }

    public static void main(String... strings) {
        new RouteTest();
    }

}

class M {
    public static Integer[] sp = { 2, 1 };
    public static Integer[] ep = { 8, 6 };
    public static int mx = 9;
    public static int my = 9;

    public static Integer[][] a =
           // 0, 1, 2, 3, 4, 5, 6, 7, 8, 9
          { { 0, 0, 1, 0, 0, 0, 0, 0, 0, 0 }, // 0
            { 0, 1, 0, 0, 0, 1, 0, 0, 1, 0 }, // 1
            { 0, 0, 0, 1, 0, 0, 1, 0, 1, 0 }, // 2
            { 0, 1, 1, 0, 1, 0, 1, 0, 1, 0 }, // 3
            { 0, 0, 0, 0, 0, 0, 1, 0, 1, 0 }, // 4
            { 0, 1, 1, 1, 1, 0, 0, 0, 1, 0 }, // 5
            { 0, 1, 1, 1, 0, 0, 0, 0, 0, 0 }, // 6
            { 0, 1, 1, 1, 0, 1, 1, 1, 0, 1 }, // 7
            { 0, 1, 1, 1, 0, 1, 0, 0, 0, 1 }, // 8
            { 0, 0, 0, 0, 0, 1, 0, 1, 1, 1 } };// 9

}
