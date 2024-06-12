package io.github.van1164.result;

public class HttpReq {
    Boolean found;
    Integer success;
    Integer fail;
    Integer total;

    public HttpReq(){
        this.found = false;
    }

    public HttpReq(Integer success, Integer fail, Integer total) {
        this.success = success;
        this.fail = fail;
        this.total = total;
        this.found = true;
    }



    public Integer getSuccess() {
        return success;
    }

    public Integer getFail() {
        return fail;
    }

    public Integer getTotal() {
        return total;
    }
}
