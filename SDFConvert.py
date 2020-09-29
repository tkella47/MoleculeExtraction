### Gaussian to SDF via OpenBabel
import subprocess
from subprocess import PIPE

obabelexe = r'C:\Program Files\OpenBabel-3.1.1\obabel.exe'
filename = r'C:\Users\tommy\OneDrive\Documents\Rfile'
### C:\Users\tommy\OneDrive\Documents\Rfile\LAcid
acidList = ['AlCl3','AlH3','AlCF','AlMe3','AlEtCl2','BH3','BF3','BCl3','BBr3','BCF','BI3','SO2']
#acidList = ['\AlCl3']
writefile = r'C:\Users\tommy\OneDrive\Documents\Rfile\LALB.sdf'
open(writefile,'w').close()
for i in range(1,24):
    for j in acidList : 
        filename = r'C:\Users\tommy\OneDrive\Documents\Rfile'
        filename = filename +'\\' + str(i) + '_' + j + '_Opt_uAPFD_6311gdp_Vac_hir_nbo.log'
        #print(filename);
    #osmi is to smiles osdf is to sdf
        output = subprocess.run([obabelexe, filename, "-osdf"],check=True, stdout=PIPE, stderr=PIPE)
        #print(output.stdout.decode("utf-8").split('\t')[0])
        f = open(writefile,'a', newline='')
        #f.write(output)
        f.write(output.stdout.decode("utf-8").split('\t')[0])
    ###f.write(output.stdout.decode("utf-8").split('\t')[0])
        f.close()
print("SDF file complete")
##output = subprocess.run([obabelexe, filename, "-osmi"],check=True, stdout=PIPE, stderr=PIPE)

##print(output.stdout.decode("utf-8").split('\t')[0])
### end of code
