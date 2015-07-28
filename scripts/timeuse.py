import xlwt
import os
import string

def getfilelist():
    FileList = []
    FileNames = os.listdir(os.getcwd())
    if (len(FileNames)>0):
       for fn in FileNames:
           if 'game' in fn:
               fullfilename=os.path.join(os.getcwd(),fn)
               FileList.append(fullfilename)

    if (len(FileList)>0):
        FileList.sort()

    return FileList

def writexcel():
    global filename
    filename = os.path.join(os.getcwd(), 'timeuse.xls')
    workbook = xlwt.Workbook(encoding='utf-8')
    booksheet = workbook.add_sheet('Sheet 1', cell_overwrite_ok=True)
    filelist = getfilelist()
    filenumber = -1

    for fn in filelist:
        file = open(fn)
        try:
            i = 0
            filenumber +=1
            lines = file.readlines()
            for line in lines:
                data = string.atoi(line)
                booksheet.write(i, filenumber, data/1000000.0)
                i +=1
                booksheet.col(filenumber).width = 3333
        finally:
            file.close()

    workbook.save(filename)

def main():
    writexcel()

if __name__ == "__main__":
     main()