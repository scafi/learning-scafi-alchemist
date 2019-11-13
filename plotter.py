#!/usr/bin/env python

################################################################################
########################### Script background info #############################
################################################################################

# - https://stackoverflow.com/questions/4455076/how-to-access-the-ith-column-of-a-numpy-multidimensional-array

###############################################################################
############################## Script imports #################################
###############################################################################

import sys
import numpy as np
import matplotlib.pyplot as plt
import os
from os import listdir
from os.path import isfile, join
import re
import pylab as pl                   # frange
import math                          # isnan, isinf, ceil
import pprint
from collections import defaultdict
import ruamel.yaml as yaml
from textwrap import wrap
#import copy  # copy.deepcopy(myDict)
#import fnmatch # for fnmatch.fnmatch(str,glob)

#################################################################################
############################## Script functions #################################
#################################################################################

# returns: list of full paths of files (under directory `basedir`) filtered through a file prefix (`basefn`)
def get_data_files(basedir, fileregex):
  return [join(basedir,p) for p in listdir(basedir) if isfile(join(basedir,p)) and re.match(fileregex,p)]

def remove_tuple_item_at_index(tpl,i):
  return tpl[0:i]+tpl[(i+1):len(tpl)]

# configs: list where each config is an N-dim tuple of (dim,val) tuples   
# returns: dict where keys are (dims-dim) and values are lists of configs
def group_by_varying_values_of(dim, configs):
  res = defaultdict(list)
  for config in configs:
    configWithoutDim = tuple([d for d in config if d[0]!=dim])
    res[configWithoutDim].append(config)
  return res
  
# Globals used
#   - t_start: beginning of time
#   - t_step: length of a bucket
#def bucket_pos(value):
#  math.ceil((value-t_start)/t_step)-1

# Returns a dict
def process_files(filepaths):
  return dict([process_file(fp) for fp in filepaths])
  
# Returns a pair (id,matrix) for each parsed file
def process_file(filepath):
  print("\n>>> Processing file: " + filepath)
  # Open file handle
  fh = open(filepath, "r")
  
  # Deduce some info from file name
  parts = re.findall('_+([^-]+)-(\d+\.\d+)', filepath.replace(join(basedir,basefn),''))
  parts = map(lambda p: (p[0],format(float(p[1]),'.6f').rstrip('0')), parts)
  parts = tuple(parts) # this must be hashable (and lists are not)
  print("Dimensions: " + "; ".join(map(lambda x: str(x), parts)))
  parts_suffix = "_".join(map("-".join,parts))
  title = "; ".join(map("=".join,parts))
  
  # Gets the matrix (time X exports) from file content
  # | time | export1 | ... | exportN |
  # ----------------------------------
  # |  t1  |    .    | ... |    .    |
  # | .... |   ...   | ... |   ...   |
  # |  tK  |    .    | ... |    .    |
  # ----------------------------------
  matrix = process_file_content(fh)
  dimMatrix = matrix.transpose()
  
  # Closes file handle
  fh.close()
  
  return (parts, dimMatrix)
  
def process_file_content(filehandle):
  # Read data
  lines = filehandle.readlines()
  # Removes empty and comment lines and maps to float
  data_rows = np.array([map(float, s.strip().split(" ")) for s in lines if len(s)>0 and s[0]!="#"], dtype='float')
  return data_rows

def do_bucketize(contents, nbuckets=100, start=None, end=None):
  res = dict()
  for config, content in contents.items():
    time = content[0]
    if start==None:
      start = time[0]
    if end==None:
      end = time[-1]
    time_bins = np.linspace(start,end,nbuckets)
    hist = np.histogram(time, time_bins)
    # for ncol,data in enumerate(content):
  # INCOMPLETE DEFINITION
  return res
  
def merge_samples(contents, configs):
  res = dict()
  for config,sconfigs in configs.items():
    nsamples = len(sconfigs)
    print("CONFIGURATION: " + str(config) + " has " + str(nsamples) + " samples.")
    
    matrices = [contents[sample_config] for sample_config in sconfigs]
    time = map(lambda x: round(x), matrices[0][0]) # time should be the same for all
    matrices = map(lambda l: l[1:], matrices) # skips the time dimension for each sample
    # Assumption: the position of values in matrices reflects the time in a consistent manner
    
    # Printing statistics
    #nplots = len(the_plots_labels) 
    #stats = dict()
    #for expdim in range(0,nplots-1): # without 'time', which should be at index 0
    #  for m in matrices:
    #    curdata = m[expdim]
    #    curstats = stats.get(expdim, np.zeros(len(curdata)))
    #    stats[expdim] = curstats + curdata
    #print(stats)
    
    merged = reduce(lambda a,b: a+b, matrices)
    merged = map(lambda x: x/nsamples, merged)
    merged.insert(0,time) # reinserts time
    res[config] = merged
  return res
  
def plot(config,content):
  title = map("=".join,config)
  if doWrap is not None: title = wrap("    ".join(title), 30)
  title = "\n".join([s.strip() for k,s in enumerate(title) if k not in excluded_titles])
  parts_suffix = "_".join(map("-".join,config))
  for nf, pformat in enumerate(the_plots_formats):
    plt.figure() # (figsize=(10,10), dpi=80)
    plt.xlabel(the_plots_labels[pformat[0]])
    plt.ylabel(y_labels[nf] if len(y_labels)>nf else "")
    maxy = float("-inf")
    for k in range(1,len(pformat)): # skip x-axis which is at pos 0
      plt.plot(content[pformat[0]], content[pformat[k]], color=the_plots_colors[nf][pformat[k]], label=the_plots_labels[pformat[k]], linewidth=line_widths[nf][k])
      maxy = max(maxy, np.nanmax(content[pformat[k]]))
    maxy = min(maxy+10, limitPlotY[nf])
    if forceLimitPlotY.has_key(nf): maxy = forceLimitPlotY[nf]
    axes = plt.gca()
    axes.set_ylim(ymax = maxy, ymin = startPlotY[nf])  
    if forceLimitPlotX.has_key(nf): axes.set_xlim(xmax = forceLimitPlotX[nf])  
    plt.legend(loc= legendPosition[nf] if legendPosition.has_key(nf) else 'upper right', prop={'size': legend_size})
    t = plt.title(title_prefix[nf]+title)
    plt.subplots_adjust(top=.84) 
    suffix = (suffixes[nf] if suffixes.has_key(nf) else "".join(map(str,pformat))) + "_" + parts_suffix
    savefn = outdir+basefn+"_"+ suffix +".png"
    print("SAVE: " + savefn)
    plt.tight_layout()
    plt.savefig(savefn, bbox_inches='tight', pad_inches = 0)  
    plt.close() 

pp = pprint.PrettyPrinter(indent=4) # for logging purposes

#######################################################################################
############################## Script configuration ###################################
#######################################################################################

sampling = True  # tells if there is a 'random' dimension for sampling
bucketize = False # tells if there is a 'time' dimension to be split into buckets
do_aggr_plotting = True
forceLimitPlotY = None
forceLimitPlotX = None
doWrap = None
limitPlotY = {}
fill_between = []
default_colors = ["black","red","blue","green"]
the_plots_labels = []
the_plots_formats = []
the_plots_colors = []
line_widths = []
title_prefix = ""

###################################################################################
############################## Script preparation #################################
###################################################################################

script = sys.argv[0]
if len(sys.argv)<3:
  print("USAGE: plotter2 <plotConfig> <basedir> <fileregex> <basefn>")
  exit(0)

plotconfig = sys.argv[1]
basedir = sys.argv[2]
fileregex = sys.argv[3]
basefn = sys.argv[4]
outdir = os.path.join(basedir, "imgs/")
if not os.path.exists(outdir):
  os.makedirs(outdir)
  
files = get_data_files(basedir,fileregex)

print("Executing script: basedir=" + basedir + "\t fileregex=" + fileregex)
print("Files to be processed: " + str(files))
print("Output directory for graphs: " + str(outdir))
print("Loading plot configurartion: " + str(plotconfig))

############################# Plot configuration

def parse_sim_option(pc, option, default=None):
    opt = pc.get(option)
    if type(opt) is dict:
        defval = opt[opt.keys()[-1]]
        opt = defaultdict(lambda: defval, opt)
    elif type(opt) is list:
        defval = opt[-1]
        opt = defaultdict(lambda: defval, dict(enumerate(opt)))
    elif not opt:
        opt = defaultdict(lambda: default)
    else: # single value
        defval = opt
        opt = defaultdict(lambda: defval)
    print(option + " >> " + str(opt))
    return opt

with open(plotconfig, 'r') as stream:
    try:
        pc = yaml.load(stream, Loader=yaml.Loader)
        the_plots_labels = pc['the_plots_labels']
        the_plots_formats = pc['the_plots_formats']
        the_plots_colors = parse_sim_option(pc, 'the_plots_colors')
        suffixes = parse_sim_option(pc, 'file_suffixes')
        line_widths = parse_sim_option(pc, 'line_widths')
        limitPlotY = parse_sim_option(pc, 'limit_plot_y', float('inf'))
        startPlotY = parse_sim_option(pc, 'start_plot_y', 0)
        forceLimitPlotY = parse_sim_option(pc, 'force_limit_plot_y')
        forceLimitPlotX = parse_sim_option(pc, 'force_limit_plot_x')
        legendPosition = parse_sim_option(pc, 'legend_position')
        y_labels = pc.get('y_labels',[])
        legend_size = pc.get('legend_size',10)
        sampling = pc.get('sampling', False)
        sampling_dim = pc.get('samplingField', 'random')
        excluded_titles = pc.get('excluded_titles',[])
        title_prefix = parse_sim_option(pc, 'title_prefix', '')
        doWrap = pc.get('do_wrap')
        plt.rcParams.update({'font.size': pc.get('font_size', 14)})
    except yaml.YAMLError as exc:
        print(exc)
        exit(1)

############################# Script logic

print('*************************')
print('*** PER FILE PLOTTING ***')
print('*************************')

# CONTENTS: a dict from file descriptors (dimension k/v pairs) to file contents (matrix data)
#   Dictionary {key => matrix}
#   file1 [d1=A  d2=B ] => export1=[...], ..., exportK=[...]
#   file2 [d1=A' d2=B ] => export1=[...], ..., exportK=[...]  
#   file3 [d1=A  d2=B'] => export1=[...], ..., exportK=[...]
#   file4 [d1=A' d2=B'] => export1=[...], ..., exportK=[...]
contents = process_files(files)

# CONFIGURATIONS
#   file1_2 [d1=*, d2=B ] => export1=[...], ..., exportK=[...]
#   file3_4 [d1=*, d2=B'] => export1=[...], ..., exportK=[...]
configs = contents.keys() # List of configs, where each config is an N-dim tuple of (k,v) tuples
if sampling:
  # Let's group configurations (individual datasets) into groups where only a sampling dimension varies
  # sconfigs is a dict where keys are (dims-'random') and values are lists of configs
  sconfigs = group_by_varying_values_of(sampling_dim, configs)
  
  merged_contents = merge_samples(contents, sconfigs)
  for title,content in merged_contents.items():
    plot(title,content)
else:
    for title,content in contents.items(): plot(title,content)
  

if bucketize:
  contents = do_bucketize(contents)
  

