package lsann.attrib;

public class TestDerived2 extends TestDerivedClass {
    @TestAttrib4
    public void setMe(String arg) {
    }

    public void setMeArg(@TestAttrib4 String arg) {
    }

    @TestAttrib4
    String member ="aaa";

}
