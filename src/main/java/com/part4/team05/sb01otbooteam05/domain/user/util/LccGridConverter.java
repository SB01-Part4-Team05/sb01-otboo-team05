package com.part4.team05.sb01otbooteam05.domain.user.util;

public class LccGridConverter {
  // 제공받은 파일 속 좌표와 위경도 전환 코드
  private static final double RE = 6371.00877; //지도반경
  private static final double GRID = 5.0; //격자 간격
  private static final double SLAT1 = 30.0; //표준위도1
  private static final double SLAT2 = 60.0; //표준위도2
  private static final double OLON = 126.0; //기준점 경도
  private static final double OLAT = 38.0; //기준점 위도
  private static final double XO = 210 / GRID; //기준점 X좌표
  private static final double YO = 675 / GRID; //기준점 Y좌표

  public static XY toGrid(double latitude, double longitude) {
    double DEGRAD = Math.PI / 180.0;

    double re = RE / GRID;
    double slat1 = SLAT1 * DEGRAD;
    double slat2 = SLAT2 * DEGRAD;
    double olon = OLON * DEGRAD;
    double olat = OLAT * DEGRAD;

    double sn = Math.tan(Math.PI * 0.25 + slat2 * 0.5) / Math.tan(Math.PI * 0.25 + slat1 * 0.5);
    sn = Math.log(Math.cos(slat1) / Math.cos(slat2)) / Math.log(sn);
    double sf = Math.tan(Math.PI * 0.25 + slat1 * 0.5);
    sf = Math.pow(sf, sn) * Math.cos(slat1) / sn;
    double ro = Math.tan(Math.PI * 0.25 + olat * 0.5);
    ro = re * sf / Math.pow(ro, sn);

    double ra = Math.tan(Math.PI * 0.25 + latitude * DEGRAD * 0.5);
    ra = re * sf / Math.pow(ra, sn);
    double theta = longitude * DEGRAD - olon;
    if (theta > Math.PI) theta -= 2.0 * Math.PI;
    if (theta < -Math.PI) theta += 2.0 * Math.PI;
    theta *= sn;

    int x = (int) (ra * Math.sin(theta) + XO + 0.5);
    int y = (int) (ro - ra * Math.cos(theta) + YO + 0.5);

    return new XY(x, y);
  }

  // 결과 반환용 내부 클래스
  public static class XY {
    public final int x;
    public final int y;
    public XY(int x, int y) {
      this.x = x;
      this.y = y;
    }
  }
}
