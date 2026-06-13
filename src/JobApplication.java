public class JobApplication {
    private String company;
    private String date;
    private String positionName;
    private String resume;
    private Status status;

    public JobApplication(String company, String date, String positionName, String resume, Status status) {
        this.company = company;
        this.date = date;
        this.positionName = positionName;
        this.resume = resume;
        this.status = status;
    }

    // getters/setters
    public String getCompany() { 
        return company; 
    }
    public String getDate() { 
        return date; 
    }
    public String getPositionName() { 
        return positionName; 
    }
    public String getResume() { 
        return resume; 
    }
    public Status getStatus() { 
        return status; 
    }

    public void setCompany(String company) { 
        this.company = company; 
    }
    public void setDate(String date) { 
        this.date = date; 
    }
    public void setPositionName(String positionName) { 
        this.positionName = positionName; 
    }
    public void setResume(String resume) { 
        this.resume = resume; 
    }
    public void setStatus(Status status) { 
        this.status = status; 
    }
}
