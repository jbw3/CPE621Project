package edu.uah.cpe.amdessapp;

import com.androidplot.xy.XYSeries;

import java.util.LinkedList;

public class DynamicXYSeries implements XYSeries
{
    private String title;
    private LinkedList<Number> data = new LinkedList<>();
    private long maxSize = 0;

    public DynamicXYSeries(String seriesTitle)
    {
        title = seriesTitle;
    }

    public DynamicXYSeries(String seriesTitle, long seriesMaxSize)
    {
        title = seriesTitle;
        maxSize = seriesMaxSize;
    }

    public void addValue(Number value)
    {
        data.add(value);
        if (maxSize > 0 && data.size() > maxSize)
        {
            data.removeFirst();
        }
    }

    public void clear()
    {
        data.clear();
    }

    @Override
    public String getTitle()
    {
        return title;
    }

    @Override
    public int size()
    {
        return data.size();
    }

    @Override
    public Number getX(int index)
    {
        return index;
    }

    @Override
    public Number getY(int index)
    {
        return data.get(index);
    }
}
